package com.minequality.fabric.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityManager;
import com.minequality.quality.QualityModifier;
import com.minequality.quality.QualityTier;

/**
 * Quan ly vong doi pham chat vat pham tu phia Server cua Fabric Loader.
 * Dam bao pham chat chi duoc boc tham va luu vao CustomData duy nhat mot lan boi Server,
 * dong bo truc tiep cac component goc cua Minecraft (Unbreakable, DamageResistant),
 * va duy tri hieu ung trang thai (Status Effects: Haste II, Full Set Bonus, Junk debuffs).
 */
public final class FabricServerItemManager {

    private static boolean registered = false;
    private static long lastEmergencyTick = -6000;

    private FabricServerItemManager() {
    }

    /**
     * Dang ky su kien Server Tick de quan ly pham chat vat pham va hieu ung trang thai.
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        tryRegisterServerTickSafe();
    }

    /**
     * Kiem tra trang thai da dang ky.
     */
    public static boolean isRegistered() {
        return registered;
    }

    /**
     * Dat lai trang thai dang ky (dung trong kiem thu).
     */
    public static synchronized void resetRegistration() {
        registered = false;
    }

    private static void tryRegisterServerTickSafe() {
        try {
            Class.forName("net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents");
            registerActualServerTick();
        } catch (Throwable ignored) {
            // Môi trường kiểm thử đơn vị không có Server Lifecycle API
        }
    }

    private static void registerActualServerTick() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server == null || server.getPlayerList() == null) {
                return;
            }

            int currentTick = server.getTickCount();

            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player == null) {
                    continue;
                }

                // 1. Kiem tra toan bo tui do (Inventory)
                net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
                if (inventory != null) {
                    int size = inventory.getContainerSize();
                    for (int i = 0; i < size; i++) {
                        net.minecraft.world.item.ItemStack stack = inventory.getItem(i);
                        ensureQualityAssigned(stack);
                    }
                }

                // 2. Kiem tra vat pham dang cam tren con tro chuot (Carried Stack) khi vua lay tu o che tao
                if (player.containerMenu != null) {
                    net.minecraft.world.item.ItemStack carried = player.containerMenu.getCarried();
                    ensureQualityAssigned(carried);

                    // 3. Kiem tra cac o container khac nhung TUYET DOI BO QUA ResultSlot
                    for (net.minecraft.world.inventory.Slot slot : player.containerMenu.slots) {
                        if (slot == null || slot instanceof net.minecraft.world.inventory.ResultSlot) {
                            // Bo qua o xem truoc che tao de an pham chat
                            continue;
                        }
                        ensureQualityAssigned(slot.getItem());
                    }
                }

                // 4. Cap nhat hieu ung trang thai (Held Item Passive & Full Set Armor Bonus)
                updatePlayerEffects(player, currentTick);
            }
        });
    }

    /**
     * Kiem tra va gan pham chat cho vat pham neu hop le va chua tung duoc gan.
     * Dong thoi tu dong dong bo (Self-Healing) Unbreakable va Fireproof neu vat pham da co pham chat.
     */
    public static boolean ensureQualityAssigned(net.minecraft.world.item.ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        try {
            net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id == null) {
                return false;
            }
            String itemId = id.toString();
            if (!QualityItemFilter.isEligible(itemId)) {
                return false;
            }

            // Kiem tra xem stack da co the "minequality" chua
            net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null) {
                net.minecraft.nbt.CompoundTag rootTag = customData.copyTag();
                if (rootTag.contains("minequality")) {
                    // Da co pham chat: Kiem tra va tu dong dong bo Unbreakable va Fireproof (Self-healing)
                    ItemQualityData currentData = FabricTooltipHandler.readQualityDataOnly(stack);
                    if (currentData != null) {
                        applyNativeComponents(stack, currentData);
                    }
                    return false;
                }
            }

            // Neu chua co: Server tien hanh roll DUY NHAT 1 LAN va luu truc tiep vao DataComponents.CUSTOM_DATA
            ItemQualityData rolled = ItemQualityData.of(QualityManager.rollQuality());
            final ItemQualityData finalRolled = rolled;
            net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
                net.minecraft.nbt.CompoundTag qTag = new net.minecraft.nbt.CompoundTag();
                qTag.putString("tier", finalRolled.tier().name());
                qTag.putInt("affinity", finalRolled.affinity());
                qTag.putInt("reforge_count", finalRolled.reforgeCount());
                qTag.putBoolean("fireproof", finalRolled.fireproof());
                qTag.putBoolean("unbreakable", finalRolled.unbreakable());
                tag.put("minequality", qTag);
            });

            // Gan truc tiep component goc cua Minecraft (Unbreakable va DamageResistant)
            applyNativeComponents(stack, finalRolled);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Gan truc tiep cac thanh phan goc cua Minecraft 26.3 vao ItemStack:
     * - Unbreakable: Khoa do ben vinh cuu, tu dong hoi phuc do ben neu da bi sut me.
     * - DamageResistant: Chong chiu 100% sat thuong lua va dung nham y het Netherite.
     * - AttributeModifiers: Gan truc tiep chi so Mau toi da (Max Health), Giap, Do cung giap, Khang day lui goc.
     */
    public static void applyNativeComponents(net.minecraft.world.item.ItemStack stack, ItemQualityData data) {
        if (stack == null || stack.isEmpty() || data == null) {
            return;
        }
        try {
            // 1. Xu ly Do ben Bat Tu (Unbreakable)
            if (data.unbreakable()) {
                stack.set(net.minecraft.core.component.DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);
                if (stack.getDamageValue() > 0) {
                    stack.setDamageValue(0);
                }
            } else {
                stack.remove(net.minecraft.core.component.DataComponents.UNBREAKABLE);
            }

            // 2. Xu ly Chong Chay (DamageResistant - Fire & Lava immune)
            if (data.fireproof()) {
                net.minecraft.world.item.component.DamageResistant fireResistant =
                        net.minecraft.world.item.Items.NETHERITE_INGOT.components().get(net.minecraft.core.component.DataComponents.DAMAGE_RESISTANT);
                if (fireResistant != null) {
                    stack.set(net.minecraft.core.component.DataComponents.DAMAGE_RESISTANT, fireResistant);
                }
            }

            // 3. Xu ly Thuoc tinh nguyen ban (AttributeModifiers: Max Health, Armor, Toughness, Knockback)
            net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null) {
                String itemId = id.toString();
                QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(itemId);
                applyAttributeModifiers(stack, data, category);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void applyAttributeModifiers(net.minecraft.world.item.ItemStack stack, ItemQualityData data, QualityModifier.EquipmentCategory category) {
        net.minecraft.world.entity.EquipmentSlotGroup slotGroup = switch (category) {
            case ARMOR_HELMET -> net.minecraft.world.entity.EquipmentSlotGroup.HEAD;
            case ARMOR_CHESTPLATE -> net.minecraft.world.entity.EquipmentSlotGroup.CHEST;
            case ARMOR_LEGGINGS -> net.minecraft.world.entity.EquipmentSlotGroup.LEGS;
            case ARMOR_BOOTS -> net.minecraft.world.entity.EquipmentSlotGroup.FEET;
            default -> null;
        };
        if (slotGroup == null) {
            return;
        }

        QualityModifier.ModifierStats stats = QualityModifier.getStats(data.tier(), category);

        net.minecraft.world.item.component.ItemAttributeModifiers baseModifiers = stack.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
        if (baseModifiers == null) {
            baseModifiers = stack.getItem().components().get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
        }
        if (baseModifiers == null) {
            baseModifiers = net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY;
        }

        net.minecraft.world.item.component.ItemAttributeModifiers.Builder builder = net.minecraft.world.item.component.ItemAttributeModifiers.builder();

        // Giu lai tat ca modifier goc khong thuoc namespace "minequality"
        for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : baseModifiers.modifiers()) {
            if (entry != null && entry.modifier() != null && entry.modifier().id() != null) {
                if (!"minequality".equals(entry.modifier().id().getNamespace())) {
                    builder.add(entry.attribute(), entry.modifier(), entry.slot(), entry.display());
                }
            }
        }

        // Gan chi so Mau toi da (Max Health) nguyen ban
        if (stats.healthBonus() != 0) {
            builder.add(
                    net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath("minequality", "quality_health_bonus"),
                            stats.healthBonus(),
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                    ),
                    slotGroup
            );
        }

        // Gan chi so Giap cong them (Armor)
        if (stats.armorBonus() != 0) {
            builder.add(
                    net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath("minequality", "quality_armor_bonus"),
                            stats.armorBonus(),
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                    ),
                    slotGroup
            );
        }

        // Gan chi so Do cung giap (Armor Toughness)
        if (stats.toughnessBonus() != 0) {
            builder.add(
                    net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS,
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath("minequality", "quality_toughness_bonus"),
                            stats.toughnessBonus(),
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                    ),
                    slotGroup
            );
        }

        // Gan chi so Khang day lui (Knockback Resistance)
        if (stats.knockbackResistanceBonus() != 0) {
            builder.add(
                    net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE,
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            net.minecraft.resources.Identifier.fromNamespaceAndPath("minequality", "quality_knockback_bonus"),
                            stats.knockbackResistanceBonus(),
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE
                    ),
                    slotGroup
            );
        }

        stack.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    /**
     * Cap nhat hieu ung trang thai thoi gian thuc dua theo trang bi dang cam tren tay va bo giap dang mac.
     */
    private static void updatePlayerEffects(net.minecraft.server.level.ServerPlayer player, int currentTick) {
        if (player == null || !player.isAlive()) {
            return;
        }

        // A. Xet trang bi tren tay chinh (Main Hand)
        net.minecraft.world.item.ItemStack mainHand = player.getMainHandItem();
        if (mainHand != null && !mainHand.isEmpty()) {
            net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(mainHand.getItem());
            if (id != null) {
                String itemId = id.toString();
                if (QualityItemFilter.isEligible(itemId)) {
                    ItemQualityData data = FabricTooltipHandler.readQualityDataOnly(mainHand);
                    if (data != null) {
                        QualityModifier.EquipmentCategory category = QualityItemFilter.getCategory(itemId);

                        // 1. Cong cu khai khoang Legendary: Dao nhanh II (Haste II)
                        if (data.tier() == QualityTier.LEGENDARY && category == QualityModifier.EquipmentCategory.MINING_TOOL) {
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    net.minecraft.world.effect.MobEffects.HASTE, 40, 1, true, false, true
                            ));
                        }

                        // 2. Cong cu khai khoang Junk: Dao cham I (Mining Fatigue I)
                        if (data.tier() == QualityTier.JUNK && category == QualityModifier.EquipmentCategory.MINING_TOOL) {
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    net.minecraft.world.effect.MobEffects.MINING_FATIGUE, 40, 0, true, false, true
                            ));
                        }

                        // 3. Vu khi can chien Junk: Suy yeu I (Weakness I)
                        if (data.tier() == QualityTier.JUNK && category == QualityModifier.EquipmentCategory.MELEE_WEAPON) {
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    net.minecraft.world.effect.MobEffects.WEAKNESS, 40, 0, true, false, true
                            ));
                        }
                    }
                }
            }
        }

        // B. Xet Full Set Giap (4 manh: Mu, Ao, Quan, Giay)
        net.minecraft.world.item.ItemStack head = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        net.minecraft.world.item.ItemStack chest = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        net.minecraft.world.item.ItemStack legs = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        net.minecraft.world.item.ItemStack feet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);

        ItemQualityData headData = FabricTooltipHandler.readQualityDataOnly(head);
        ItemQualityData chestData = FabricTooltipHandler.readQualityDataOnly(chest);
        ItemQualityData legsData = FabricTooltipHandler.readQualityDataOnly(legs);
        ItemQualityData feetData = FabricTooltipHandler.readQualityDataOnly(feet);

        if (headData != null && headData.tier() == QualityTier.LEGENDARY &&
            chestData != null && chestData.tier() == QualityTier.LEGENDARY &&
            legsData != null && legsData.tier() == QualityTier.LEGENDARY &&
            feetData != null && feetData.tier() == QualityTier.LEGENDARY) {

            // 1. Khang lua vinh cuu
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 40, 0, true, false, true
            ));

            // 2. Tu hoi phuc thu dong moi 60 ticks (3 giay): Hoi 1 mau va nạp 1 diem thanh doi
            if (currentTick % 60 == 0) {
                // Hoi mau thu dong neu chua day
                if (player.getHealth() < player.getMaxHealth() && player.getHealth() > 0.0f) {
                    player.heal(1.0f);
                }
                // Tu dong nap thanh doi va do bao hoa neu can
                net.minecraft.world.food.FoodData foodData = player.getFoodData();
                if (foodData != null && (foodData.needsFood() || foodData.getFoodLevel() < 20)) {
                    foodData.eat(1, 0.5f);
                }
            }

            // 3. Cuu sinh khan cap khi mau < 30%
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            if (health > 0.0f && (health / maxHealth) < 0.30f) {
                if (currentTick - lastEmergencyTick >= 1200 || lastEmergencyTick < 0) {
                    lastEmergencyTick = currentTick;
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.RESISTANCE, 160, 0, false, true, true
                    ));
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.REGENERATION, 160, 1, false, true, true
                    ));
                }
            }
        }
    }
}
