package com.minequality.neoforge.event;

import com.minequality.quality.ItemQualityData;
import com.minequality.quality.QualityItemFilter;
import com.minequality.quality.QualityTier;

/**
 * Hook xu ly logic ren lai (Reforge) tren De (Anvil) cho NeoForge Loader.
 * Xu ly tinh toan chi phi kinh nghiem (EXP Level), chan tran toi da 30 cap,
 * kiem tra nguyen lieu hop le, co che bao hiem Ngoc Luc Bao (Emerald Insurance)
 * va co che vo do trong Vung Mao Hiem theo tai lieu thiet ke.
 */
public final class NeoForgeAnvilHandler {

    private static boolean registered = false;

    private NeoForgeAnvilHandler() {
    }

    /**
     * Dang ky hook Anvil vao he thong Event Bus cua NeoForge.
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
     * Ban ghi ket qua xu ly ren tren De cho NeoForge.
     *
     * @param canReforge      Trang bi va nguyen lieu co hop le de thuc hien ren hay khong.
     * @param expCost         So cap do kinh nghiem (EXP Level) bi tieu hao (toi da 30 cap).
     * @param materialCost    So luong nguyen lieu tieu hao (mac dinh 1).
     * @param emeraldConsumed Xac nhan vien Ngoc Luc Bao co bi nat vun de cuu song trang bi hay khong.
     * @param broken          Xac nhan trang bi co bi vo nat do tach trong Vung Mao Hiem hay khong.
     * @param upgraded        Xac nhan co nang cap len pham chat cao hon hay khong.
     * @param pityTriggered   Xac nhan co kich hoat co che van may (Pity) hay khong.
     * @param resultingData   Du lieu pham chat moi cua trang bi (null neu vat pham bi vo nat).
     * @param failureReason   Ly do tu choi neu khong the ren (null neu hop le).
     */
    public record AnvilReforgeOutput(
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
        public static AnvilReforgeOutput rejected(String reason) {
            return new AnvilReforgeOutput(false, 0, 0, false, false, false, false, null, reason);
        }
    }

    /**
     * Kiem tra xem mot giao dich ren co the thuc hien duoc hay khong va tinh toan chi phi EXP du kien.
     *
     * @param itemId         Dinh danh vat pham o o 1.
     * @param currentData    Du lieu pham chat hien tai cua vat pham.
     * @param materialId     Dinh danh nguyen lieu o o 2.
     * @param materialCount  So luong nguyen lieu hien co.
     * @param playerExpLevel Cap do EXP hien tai cua nguoi choi.
     * @return true neu du dieu kien de nguoi choi thuc hien ren.
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
            return false; // Da dat bac cao nhat
        }
        if (!QualityItemFilter.isReforgeMaterial(itemId, materialId) || materialCount < 1) {
            return false;
        }
        int expCost = QualityItemFilter.calculateReforgeCost(itemId, currentData.reforgeCount());
        return playerExpLevel >= expCost;
    }

    /**
     * Tinh toan chi phi EXP cho lan ren hien tai (ap dung muc tran toi da 30 cap de tranh loi Too Expensive).
     *
     * @param itemId       Dinh danh vat pham.
     * @param reforgeCount So lan ren that bai tich luy.
     * @return Chi phi cap do EXP (1 - 30).
     */
    public static int getExpCost(String itemId, int reforgeCount) {
        return QualityItemFilter.calculateReforgeCost(itemId, reforgeCount);
    }

    /**
     * Xu ly hanh dong ren tren De khi nguoi choi lay vat pham o ket qua.
     *
     * @param itemId              Dinh danh vat pham o o ben trai.
     * @param currentData         Du lieu pham chat cua vat pham o o ben trai.
     * @param materialId          Dinh danh nguyen lieu o o ben phai.
     * @param materialCount       So luong nguyen lieu hien co o o ben phai.
     * @param playerExpLevel      Cap do EXP hien tai cua nguoi choi.
     * @param hasEmeraldInsurance Xac nhan nguoi choi co kep Ngoc Luc Bao de bao hiem hay khong.
     * @return Ket qua chi tiet sau khi thuc hien ren.
     */
    public static AnvilReforgeOutput processReforge(
            String itemId,
            ItemQualityData currentData,
            String materialId,
            int materialCount,
            int playerExpLevel,
            boolean hasEmeraldInsurance
    ) {
        if (itemId == null || currentData == null || materialId == null) {
            return AnvilReforgeOutput.rejected("Thong tin ren khong hop le hoac bi null.");
        }
        if (!QualityItemFilter.isEligible(itemId)) {
            return AnvilReforgeOutput.rejected("Vat pham khong thuoc danh sach trang bi duoc phep ren.");
        }
        if (currentData.tier() == QualityTier.LEGENDARY) {
            return AnvilReforgeOutput.rejected("Trang bi da dat pham chat Huyen Thoai toi thuong, khong the ren them.");
        }
        if (!QualityItemFilter.isReforgeMaterial(itemId, materialId)) {
            return AnvilReforgeOutput.rejected("Nguyen lieu khong phu hop voi loai vat lieu cua trang bi.");
        }
        if (materialCount < 1) {
            return AnvilReforgeOutput.rejected("Khong du so luong nguyen lieu yeu cau de ren.");
        }

        int expCost = QualityItemFilter.calculateReforgeCost(itemId, currentData.reforgeCount());
        if (playerExpLevel < expCost) {
            return AnvilReforgeOutput.rejected("Nguoi choi khong du cap do kinh nghiem (Yeu cau: " + expCost + " cap).");
        }

        // Thuc hien logic ren tren tang Core qua applyReforge
        ItemQualityData.ReforgeOutcome outcome = currentData.applyReforge(hasEmeraldInsurance);

        if (outcome.isBroken()) {
            // Trang bi bi vo nat do tach trong Danger Zone ma khong co bao hiem
            return new AnvilReforgeOutput(
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

        return new AnvilReforgeOutput(
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
