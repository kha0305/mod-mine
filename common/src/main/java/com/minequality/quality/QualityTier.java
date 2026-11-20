package com.minequality.quality;

/**
 * Định nghĩa các bậc phẩm chất của vật phẩm trang bị trong game.
 */
public enum QualityTier {
    JUNK("junk", "quality.mod_mine.tier.junk", 0xAAAAAA, 15),           // Màu xám - Phế phẩm (15%)
    COMMON("common", "quality.mod_mine.tier.common", 0xFFFFFF, 45),     // Màu trắng - Phổ thông (45%)
    UNCOMMON("uncommon", "quality.mod_mine.tier.uncommon", 0x55FF55, 22), // Màu xanh lá - Bền chắc (22%)
    RARE("rare", "quality.mod_mine.tier.rare", 0x55FFFF, 12),           // Màu xanh ngọc/lam - Hiếm (12%)
    EPIC("epic", "quality.mod_mine.tier.epic", 0xAA00AA, 5),            // Màu tím - Sử thi (5%)
    LEGENDARY("legendary", "quality.mod_mine.tier.legendary", 0xFFAA00, 1); // Màu cam vàng - Huyền thoại (1%)

    private final String id;
    private final String translationKey;
    private final int colorHex;
    private final int weight;

    QualityTier(String id, String translationKey, int colorHex, int weight) {
        this.id = id;
        this.translationKey = translationKey;
        this.colorHex = colorHex;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public int getColorHex() {
        return colorHex;
    }

    public int getWeight() {
        return weight;
    }
}
