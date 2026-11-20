package com.minequality.forge.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityTier;

/**
 * Xử lý sự kiện rèn lại (Reforge) trên Đe (Anvil) cho MinecraftForge Loader.
 * Kiểm soát giới hạn trần 30 cấp EXP để không bao giờ vượt ngưỡng 39 cấp Vanilla (tránh lỗi Too Expensive),
 * xử lý cơ chế vỡ đồ trong Vùng Mạo Hiểm (Danger Zone) và tính năng bảo hiểm Ngọc Lục Bảo (Emerald).
 */
public final class ForgeAnvilHandler {

    private static boolean registered = false;

    private ForgeAnvilHandler() {
    }

    /**
     * Đăng ký handler vào Event Bus của Forge.
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
    }

    public static boolean isRegistered() {
        return registered;
    }

    public static synchronized void resetRegistration() {
        registered = false;
    }

    /**
     * Bản ghi chứa kết quả xử lý rèn lại trên Đe Forge.
     *
     * @param canReforge      Trang bị và nguyên liệu có hợp lệ để rèn hay không.
     * @param expCost         Chi phí cấp độ kinh nghiệm (tối đa 30 cấp).
     * @param materialCost    Số lượng nguyên liệu rèn tiêu hao (1).
     * @param emeraldConsumed Xác nhận viên Ngọc Lục Bảo có vỡ vụn thay để cứu đồ hay không.
     * @param broken          Xác nhận trang bị có bị vỡ nát hoàn toàn hay không.
     * @param upgraded        Xác nhận có nâng cấp lên phẩm chất cao hơn hay không.
     * @param pityTriggered   Xác nhận có kích hoạt bảo hiểm vận may Pity hay không.
     * @param resultingData   Dữ liệu phẩm chất mới của trang bị (null nếu bị vỡ nát).
     * @param failureReason   Lý do từ chối nếu không thể rèn.
     */
    public record AnvilReforgeResult(
            boolean canReforge,
            int expCost,
            int materialCost,
            boolean emeraldConsumed,
            boolean broken,
            boolean upgraded,
            boolean pityTriggered,
            ItemQualityData resultingData,
            String failureReason
    ) {
        public static AnvilReforgeResult rejected(String reason) {
            return new AnvilReforgeResult(false, 0, 0, false, false, false, false, null, reason);
        }
    }

    /**
     * Kiểm tra xem giao dịch rèn có thỏa mãn điều kiện thực hiện trên Đe hay không.
     *
     * @param itemId         Định danh trang bị đặt ở ô 1.
     * @param currentData    Dữ liệu phẩm chất hiện tại của trang bị.
     * @param materialId     Định danh nguyên liệu đặt ở ô 2.
     * @param materialCount  Số lượng nguyên liệu ở ô 2.
     * @param playerExpLevel Cấp độ EXP hiện tại của người chơi.
     * @return true nếu đủ điều kiện để rèn.
     */
    public static boolean canPerformReforge(
            String itemId,
            ItemQualityData currentData,
            String materialId,
            int materialCount,
            int playerExpLevel
    ) {
        if (itemId == null || currentData == null || materialId == null) {
            return false;
        }
        if (!QualityItemFilter.isEligible(itemId)) {
            return false;
        }
        if (currentData.tier() == QualityTier.LEGENDARY) {
            return false; // Đã đạt phẩm chất cao nhất
        }
        if (!QualityItemFilter.isReforgeMaterial(itemId, materialId) || materialCount < 1) {
            return false;
        }
        int cost = QualityItemFilter.calculateReforgeCost(itemId, currentData.reforgeCount());
        return playerExpLevel >= cost;
    }

    /**
     * Tính toán chi phí cấp độ kinh nghiệm (EXP Level) cho lần rèn hiện tại.
     * Áp dụng mức trần bảo vệ tối đa 30 cấp theo tài liệu thiết kế.
     *
     * @param itemId       Định danh trang bị.
     * @param reforgeCount Số lần rèn thất bại tích lũy.
     * @return Chi phí cấp độ EXP (nằm trong đoạn [1, 30]).
     */
    public static int calculateExpCost(String itemId, int reforgeCount) {
        return QualityItemFilter.calculateReforgeCost(itemId, reforgeCount);
    }

    /**
     * Xử lý thực hiện rèn trên Đe khi người chơi bấm nhận kết quả.
     *
     * @param itemId              Định danh trang bị cần rèn.
     * @param currentData         Dữ liệu phẩm chất hiện tại.
     * @param materialId          Định danh nguyên liệu rèn.
     * @param materialCount       Số lượng nguyên liệu hiện có.
     * @param playerExpLevel      Cấp độ EXP của người chơi.
     * @param hasEmeraldInsurance Người chơi có kèm theo viên Ngọc Lục Bảo để bảo hiểm hay không.
     * @return Kết quả chi tiết của lượt rèn.
     */
    public static AnvilReforgeResult processReforge(
            String itemId,
            ItemQualityData currentData,
            String materialId,
            int materialCount,
            int playerExpLevel,
            boolean hasEmeraldInsurance
    ) {
        if (itemId == null || currentData == null || materialId == null) {
            return AnvilReforgeResult.rejected("Thong tin ren khong hop le hoac bi null.");
        }
        if (!QualityItemFilter.isEligible(itemId)) {
            return AnvilReforgeResult.rejected("Vat pham khong thuoc danh sach trang bi duoc phep ren.");
        }
        if (currentData.tier() == QualityTier.LEGENDARY) {
            return AnvilReforgeResult.rejected("Trang bi da dat pham chat Huyen Thoai toi thuong, khong the ren them.");
        }
        if (!QualityItemFilter.isReforgeMaterial(itemId, materialId)) {
            return AnvilReforgeResult.rejected("Nguyen lieu khong phu hop voi loai vat lieu cua trang bi.");
        }
        if (materialCount < 1) {
            return AnvilReforgeResult.rejected("Khong du so luong nguyen lieu yeu cau de ren.");
        }

        int expCost = QualityItemFilter.calculateReforgeCost(itemId, currentData.reforgeCount());
        if (playerExpLevel < expCost) {
            return AnvilReforgeResult.rejected("Nguoi choi khong du cap do kinh nghiem (Yeu cau: " + expCost + " cap).");
        }

        ItemQualityData.ReforgeOutcome outcome = currentData.applyReforge(hasEmeraldInsurance);

        if (outcome.isBroken()) {
            return new AnvilReforgeResult(
                    true,
                    expCost,
                    1,
                    false,
                    true,
                    false,
                    false,
                    null,
                    null
            );
        }

        ItemQualityData updatedData = outcome.updatedData();
        boolean emeraldConsumed = outcome.isEmeraldConsumed();
        boolean upgraded = outcome.isUpgraded();
        boolean pityTriggered = outcome.rawResult().pityTriggered();

        return new AnvilReforgeResult(
                true,
                expCost,
                1,
                emeraldConsumed,
                false,
                upgraded,
                pityTriggered,
                updatedData,
                null
        );
    }
}
