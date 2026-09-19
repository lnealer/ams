package org.example.am.shared.dao;

import java.util.List;
import org.example.am.shared.domain.Asset;

/**
 * Drives the 'needs attention' banner: the flag and reason are denormalised onto the asset row so
 * the search grid can render them without a join.
 */
public interface AssetAttentionDAO {

    List<Asset> getAssetsNeedingAttention(long customerId);

    int flagAsset(long assetId, String reason, String userId);

    int clearFlag(long assetId, String userId);
}
