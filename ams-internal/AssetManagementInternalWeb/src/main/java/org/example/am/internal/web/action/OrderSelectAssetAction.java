package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.OrderType;
import org.example.am.shared.service.AssetService;
import org.example.am.shared.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Chooses the asset an order replaces.
 *
 * <p>Only assets with nothing in flight against them are offered, and the choice is re-checked at
 * review, because the list can go stale while the operator is still on the page.</p>
 */
@Component("OrderSelectAssetAction")
@Scope("prototype")
public class OrderSelectAssetAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    /**
     * Records the choice, but only if it is still one of the offered assets - a submitted id that
     * is not in the list is either stale or forged.
     */
    private void applySelection(final OrderModel model) {
        for (final Asset candidate : selectableAssets) {
            if (selectedAssetId.equals(candidate.getAssetId())) {
                model.setSelectedAsset(candidate);
                model.setAssetBeingReplaced(candidate);
                return;
            }
        }
        addActionError("That asset is no longer available for this order.");
        logger.warn("User {} submitted asset {}, which is not in the selectable list",
                getUserId(), selectedAssetId);
    }

    private List<Asset> selectableAssets;
    private Long selectedAssetId;

    @Autowired
    private transient AssetService assetService;

    @Autowired
    private transient CustomerService customerService;

    public String selectAsset() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SELECT_ASSET);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        selectableAssets = assetService.getMigratableAssets(
                customerService.getCustomer(model.getCustomerId().longValue()));
        if (selectedAssetId != null) {
            applySelection(model);
        }
        storeModel(model);
        return Action.SUCCESS;
    }

    public String selectEmergencyReplacementAsset() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_EMERGENCY_REPLACEMENT);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        model.setOrderType(OrderType.EMERGENCY_REPLACEMENT);
        selectableAssets = new ArrayList<Asset>();
        for (final Asset candidate : assetService.getMigratableAssets(
                customerService.getCustomer(model.getCustomerId().longValue()))) {
            if (candidate.isEmergencyReplacementEnabled()) {
                selectableAssets.add(candidate);
            }
        }
        if (selectedAssetId != null) {
            applySelection(model);
        }
        storeModel(model);
        return Action.SUCCESS;
    }

    public List<Asset> getSelectableAssets() {
        return selectableAssets;
    }

    public void setSelectableAssets(final List<Asset> selectableAssets) {
        this.selectableAssets = selectableAssets;
    }

    public Long getSelectedAssetId() {
        return selectedAssetId;
    }

    public void setSelectedAssetId(final Long selectedAssetId) {
        this.selectedAssetId = selectedAssetId;
    }
}
