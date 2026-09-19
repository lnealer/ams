package org.example.am.shared.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.am.shared.dao.AssetAttentionDAO;
import org.example.am.shared.dao.AssetConfigDAO;
import org.example.am.shared.dao.AssetDAO;
import org.example.am.shared.dao.AssetProblemDAO;
import org.example.am.shared.dao.CustomerDAO;
import org.example.am.shared.dao.MaintenanceWindowDAO;
import org.example.am.shared.dao.NetworkChangeRequestDAO;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetActionType;
import org.example.am.shared.domain.AssetProblemType;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.domain.NetworkChangeRequest;
import org.example.am.shared.helper.AssetHelper;
import org.example.am.shared.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assembles the asset object graph from the individual DAOs.
 *
 * <p>There is no ORM doing this for us: each collection is a deliberate extra query, and the two
 * entry points differ in exactly how many of them they run. {@link #getAssetSummary} exists so that
 * a grid of two hundred rows does not trigger a thousand round trips.</p>
 */
@Service("assetService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class AssetServiceImpl implements AssetService {

    private static final Logger LOGGER = LogManager.getLogger(AssetServiceImpl.class);

    @Autowired
    private AssetDAO assetDAO;

    @Autowired
    private AssetConfigDAO assetConfigDAO;

    @Autowired
    private AssetProblemDAO assetProblemDAO;

    @Autowired
    private AssetAttentionDAO assetAttentionDAO;

    @Autowired
    private MaintenanceWindowDAO maintenanceWindowDAO;

    @Autowired
    private NetworkChangeRequestDAO networkChangeRequestDAO;

    @Autowired
    private CustomerDAO customerDAO;

    @Override
    public Asset getAssetDetail(final long customerId, final long assetId) {
        final Asset asset = assetDAO.getAsset(customerId, assetId);
        if (asset == null) {
            LOGGER.debug("Asset {} not found for customer {}", Long.valueOf(assetId),
                    Long.valueOf(customerId));
            return null;
        }
        asset.setAssetConfiguration(assetConfigDAO.getCurrentConfiguration(assetId));
        asset.setMaintenanceWindow(maintenanceWindowDAO.getMaintenanceWindow(assetId));
        asset.setOrder(assetDAO.getOrderForAsset(customerId, assetId));
        asset.setDecommission(assetDAO.getDecommissionForAsset(customerId, assetId));
        asset.setNetworkChangeRequest(getOpenRequest(assetId));
        asset.setCustomer(customerDAO.getCustomer(customerId));
        return asset;
    }

    @Override
    public Asset getAssetSummary(final long customerId, final long assetId) {
        return assetDAO.getAsset(customerId, assetId);
    }

    @Override
    public List<Asset> getMigratableAssets(final Customer customer) {
        if (customer == null) {
            return new ArrayList<Asset>();
        }
        return assetDAO.getMigratableAssetsForCustomer(customer);
    }

    @Override
    public List<AssetProblemType> getProblems(final long assetId) {
        return assetProblemDAO.getProblems(assetId);
    }

    @Override
    public List<AssetActionType> getAvailableActions(final long customerId, final long assetId) {
        return AssetHelper.getAvailableActions(getAssetDetail(customerId, assetId));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long recordAssetReturn(final long customerId, final long assetId, final String userId) {
        final Asset asset = assetDAO.getAsset(customerId, assetId);
        if (asset == null) {
            throw new IllegalArgumentException("No asset " + assetId + " for customer " + customerId);
        }
        final long assetReturnId = assetDAO.addAssetReturn(assetId, asset.getSerialNumber(), userId);
        assetAttentionDAO.flagAsset(assetId, "Hardware return in progress", userId);
        return assetReturnId;
    }

    /**
     * @return the one open change request against this asset, or {@code null}; more than one open
     *         at a time is prevented by the submit path, so the first is the only one
     */
    private NetworkChangeRequest getOpenRequest(final long assetId) {
        for (final NetworkChangeRequest request : networkChangeRequestDAO.getRequestsForAsset(assetId)) {
            if (request.isOpen()) {
                return request;
            }
        }
        return null;
    }
}
