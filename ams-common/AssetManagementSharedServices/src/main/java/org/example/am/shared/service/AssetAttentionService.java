package org.example.am.shared.service;

import java.util.List;
import org.example.am.shared.domain.Asset;

/**
 * Maintains the 'needs attention' flag operations staff triage from the dashboard.
 */
public interface AssetAttentionService {

    List<Asset> getAssetsNeedingAttention(long customerId);

    void flag(long assetId, String reason, String userId);

    void clear(long assetId, String userId);
}
