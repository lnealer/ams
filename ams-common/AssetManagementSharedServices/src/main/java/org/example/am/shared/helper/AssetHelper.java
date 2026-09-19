package org.example.am.shared.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetActionType;
import org.example.am.shared.domain.AssetStatusType;
import org.example.am.shared.domain.comparator.AssetComparator;

/**
 * Presentation-shaped helpers over {@link Asset} that would clutter the domain object itself.
 */
public final class AssetHelper {

    private AssetHelper() {
        super();
    }

    /**
     * @return the actions the internal UI may offer for this asset, in menu order
     */
    public static List<AssetActionType> getAvailableActions(final Asset asset) {
        final List<AssetActionType> actions = new ArrayList<AssetActionType>();
        if (asset == null) {
            return actions;
        }
        if (asset.isCanModifyConfig()) {
            actions.add(AssetActionType.MODIFY_CONFIG);
        }
        if (asset.getAssetConfiguration() != null) {
            actions.add(AssetActionType.COMPARE_CONFIG);
        }
        if (asset.isCanMove()) {
            actions.add(AssetActionType.MOVE);
        }
        if (asset.isEmergencyReplacementEnabled() && asset.isInService()) {
            actions.add(AssetActionType.REPLACE);
        }
        if (asset.isCanDecommission()) {
            actions.add(AssetActionType.DECOMMISSION);
        }
        if (asset.isCanCreateRma()) {
            actions.add(AssetActionType.RMA);
        }
        return actions;
    }

    /**
     * @return the assets sorted the way the search grid renders them by default
     */
    public static List<Asset> sortForDisplay(final List<Asset> assets) {
        final List<Asset> sorted = new ArrayList<Asset>(assets == null
                ? Collections.<Asset>emptyList() : assets);
        Collections.sort(sorted, new AssetComparator());
        return sorted;
    }

    public static List<Asset> filterByStatus(final List<Asset> assets, final AssetStatusType status) {
        final List<Asset> matched = new ArrayList<Asset>();
        if (assets == null) {
            return matched;
        }
        for (final Asset asset : assets) {
            if (status == null || status.equals(asset.getAssetStatusType())) {
                matched.add(asset);
            }
        }
        return matched;
    }

    /**
     * @return the short summary shown under the asset tag in the grid
     */
    public static String getSummaryLine(final Asset asset) {
        if (asset == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        if (asset.getAssetType() != null) {
            builder.append(asset.getAssetType().getDescription());
        }
        if (asset.getSerialNumber() != null) {
            appendSeparator(builder);
            builder.append("S/N ").append(asset.getSerialNumber());
        }
        if (asset.getAssetStatusType() != null) {
            appendSeparator(builder);
            builder.append(asset.getAssetStatusType().getDescription());
        }
        return builder.toString();
    }

    private static void appendSeparator(final StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(" | ");
        }
    }
}
