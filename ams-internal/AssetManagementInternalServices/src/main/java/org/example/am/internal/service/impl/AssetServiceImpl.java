package org.example.am.internal.service.impl;

import java.util.List;

import org.example.am.internal.service.AssetService;
import org.example.am.internal.service.dao.AssetDAO;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.helper.AssetHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("internalAssetService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetDAO assetDAO;

    @Override
    public List<Asset> getAttentionQueue(final int maxRows) {
        return assetDAO.getAssetsNeedingAttention(maxRows);
    }

    @Override
    public List<Asset> getAssetsAtZipCode(final String zipCode) {
        if (zipCode == null || zipCode.trim().length() == 0) {
            return java.util.Collections.<Asset>emptyList();
        }
        return AssetHelper.sortForDisplay(assetDAO.getAssetsAtZipCode(zipCode.trim()));
    }

    @Override
    public int getInstalledAssetCount(final long customerId) {
        return assetDAO.getInstalledAssetCount(customerId);
    }
}
