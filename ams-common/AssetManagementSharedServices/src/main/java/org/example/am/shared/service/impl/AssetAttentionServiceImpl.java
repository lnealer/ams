package org.example.am.shared.service.impl;

import java.util.List;
import org.example.am.shared.dao.AssetAttentionDAO;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.service.AssetAttentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Maintains the 'needs attention' flag operations staff triage from the dashboard.
 */
@Service("assetAttentionService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AssetAttentionServiceImpl implements AssetAttentionService {

    @Autowired
    private AssetAttentionDAO assetAttentionDAO;

    @Override
    public List<Asset> getAssetsNeedingAttention(final long customerId) {
        return assetAttentionDAO.getAssetsNeedingAttention(customerId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void flag(final long assetId, final String reason, final String userId) {
        assetAttentionDAO.flagAsset(assetId, reason, userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void clear(final long assetId, final String userId) {
        assetAttentionDAO.clearFlag(assetId, userId);
    }
}
