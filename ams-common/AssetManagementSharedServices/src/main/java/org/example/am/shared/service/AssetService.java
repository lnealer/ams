package org.example.am.shared.service;

import java.util.List;

import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetActionType;
import org.example.am.shared.domain.AssetProblemType;
import org.example.am.shared.domain.Customer;

/**
 * The asset read model used by every screen that shows a single asset.
 */
public interface AssetService {

    /**
     * Loads an asset with everything the asset detail screen renders: the current configuration,
     * the maintenance window, the latest order, any open change request and any decommission.
     */
    Asset getAssetDetail(long customerId, long assetId);

    /** Identity and status only, for grids and for re-checking eligibility before a submit. */
    Asset getAssetSummary(long customerId, long assetId);

    List<Asset> getMigratableAssets(Customer customer);

    List<AssetProblemType> getProblems(long assetId);

    List<AssetActionType> getAvailableActions(long customerId, long assetId);

    long recordAssetReturn(long customerId, long assetId, String userId);
}
