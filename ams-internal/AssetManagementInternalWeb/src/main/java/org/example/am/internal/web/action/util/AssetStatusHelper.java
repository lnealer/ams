package org.example.am.internal.web.action.util;

import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetStatusType;
import org.example.am.shared.domain.DisplayStatusType;

/**
 * Maps an asset's state onto how the screens should present it.
 *
 * <p>Kept out of the JSPs so that "what colour is this row" is decided once and testably, rather
 * than by a chain of {@code c:choose} branches repeated on every page that shows an asset.</p>
 */
public final class AssetStatusHelper {

    private AssetStatusHelper() {
        super();
    }

    /**
     * @return the severity the row or badge should be styled with
     */
    public static DisplayStatusType getDisplayStatus(final Asset asset) {
        if (asset == null) {
            return DisplayStatusType.NORMAL;
        }
        if (asset.isNeedsAttention()) {
            return DisplayStatusType.ATTENTION;
        }
        if (asset.getAssetConfiguration() != null && asset.getAssetConfiguration().isMismatched()) {
            return DisplayStatusType.ERROR;
        }
        if (asset.getDecommission() != null && asset.getDecommission().isScheduled()) {
            return DisplayStatusType.WARNING;
        }
        if (asset.getOrder() != null && asset.getOrder().isOpen()) {
            return DisplayStatusType.INFORMATIONAL;
        }
        if (AssetStatusType.DECOMMISSIONED.equals(asset.getAssetStatusType())
                || AssetStatusType.CANCELLED.equals(asset.getAssetStatusType())) {
            return DisplayStatusType.INFORMATIONAL;
        }
        return DisplayStatusType.NORMAL;
    }

    /** @return the CSS class the JSPs apply to the row */
    public static String getRowStyleClass(final Asset asset) {
        final DisplayStatusType status = getDisplayStatus(asset);
        if (DisplayStatusType.ATTENTION.equals(status)) {
            return "ams-row-attention";
        }
        if (DisplayStatusType.ERROR.equals(status)) {
            return "ams-row-error";
        }
        if (DisplayStatusType.WARNING.equals(status)) {
            return "ams-row-warning";
        }
        if (DisplayStatusType.INFORMATIONAL.equals(status)) {
            return "ams-row-info";
        }
        return "ams-row-normal";
    }

    /**
     * @return a sentence explaining why the asset is highlighted, or {@code null} when it is not.
     *         Shown as the row's tooltip.
     */
    public static String getStatusExplanation(final Asset asset) {
        if (asset == null) {
            return null;
        }
        if (asset.isNeedsAttention()) {
            return asset.getAttentionReason() == null
                    ? "This asset needs attention." : asset.getAttentionReason();
        }
        if (asset.getAssetConfiguration() != null && asset.getAssetConfiguration().isMismatched()) {
            return "The stored configuration does not match what the device reports.";
        }
        if (asset.getDecommission() != null && asset.getDecommission().isScheduled()) {
            return "A decommission is scheduled for "
                    + asset.getDecommission().getScheduledDate() + ".";
        }
        if (asset.getOrder() != null && asset.getOrder().isOpen()) {
            return "Order " + asset.getOrder().getOrderNumber() + " is in progress.";
        }
        return null;
    }

    /**
     * @return {@code true} when the asset is in a state where no action should be offered at all
     */
    public static boolean isTerminal(final Asset asset) {
        if (asset == null || asset.getAssetStatusType() == null) {
            return false;
        }
        return AssetStatusType.DECOMMISSIONED.equals(asset.getAssetStatusType())
                || AssetStatusType.CANCELLED.equals(asset.getAssetStatusType());
    }
}
