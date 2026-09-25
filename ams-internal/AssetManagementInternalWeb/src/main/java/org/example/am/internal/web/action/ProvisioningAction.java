package org.example.am.internal.web.action;

import java.util.Collection;

import org.apache.struts2.Action;

import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetType;
import org.example.am.shared.domain.Order;
import org.example.am.shared.service.OrderService;
import org.example.am.shared.service.ProvisioningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The two steps that turn a placed order into a live asset.
 *
 * <p>Deliberately two screens rather than one button. Despatch and installation happen days apart,
 * and the states in between - shipped but not fitted, fitted but not yet confirmed - are what
 * operations spends its day looking at. Collapsing them would make the demo shorter and the model
 * wrong.</p>
 */
@Component("ProvisioningAction")
@Scope("prototype")
public class ProvisioningAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String assetTypeCode;
    private String trackingNumber;
    private String technicianName;
    private String notes;

    private Order order;
    private Asset asset;

    @Autowired
    private transient ProvisioningService provisioningService;

    @Autowired
    private transient OrderService orderService;

    /** Shows what is about to be despatched, so the warehouse can check it against the box. */
    public String initDespatch() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_DESPATCH_ORDER);
        if (denied != null) {
            return denied;
        }
        if (!loadOrder()) {
            return Action.INPUT;
        }
        return Action.SUCCESS;
    }

    public String despatch() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_DESPATCH_ORDER);
        if (denied != null) {
            return denied;
        }
        if (!loadOrder()) {
            return Action.INPUT;
        }
        final AssetType assetType = AssetType.lookup(assetTypeCode);
        if (assetType == null) {
            addFieldErrorAndLog("assetTypeCode", "Choose the hardware that came off the shelf.");
            return Action.INPUT;
        }
        try {
            asset = provisioningService.despatchOrder(getCurrentCustomerId().longValue(),
                    orderId.longValue(), assetType, trackingNumber, getUserId());
        } catch (final IllegalStateException refused) {
            // A business refusal, not a failure: the order moved on under this operator, most
            // likely because somebody else despatched it first.
            addActionError(refused.getMessage());
            return Action.INPUT;
        }
        addActionMessage("Despatched as " + asset.getAssetTag()
                + ". Book the installation visit next.");
        return Action.SUCCESS;
    }

    public String initComplete() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_COMPLETE_INSTALLATION);
        if (denied != null) {
            return denied;
        }
        if (!loadOrder()) {
            return Action.INPUT;
        }
        return Action.SUCCESS;
    }

    public String complete() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_COMPLETE_INSTALLATION);
        if (denied != null) {
            return denied;
        }
        if (!loadOrder()) {
            return Action.INPUT;
        }
        final boolean completed;
        try {
            completed = provisioningService.completeInstallation(
                    getCurrentCustomerId().longValue(), orderId.longValue(),
                    technicianName, notes, getUserId());
        } catch (final IllegalStateException refused) {
            addActionError(refused.getMessage());
            return Action.INPUT;
        }
        if (completed) {
            addActionMessage("Installation recorded. The asset is now active.");
        } else {
            addActionMessage("That installation was already closed; nothing was changed.");
        }
        loadOrder();
        return Action.SUCCESS;
    }

    /** @return {@code false} when there is no such order for the customer being worked on */
    private boolean loadOrder() throws Exception {
        if (getCurrentCustomerId() == null) {
            addActionError("Choose a customer first.");
            return false;
        }
        if (orderId == null) {
            addActionError("Choose an order.");
            return false;
        }
        order = orderService.getOrderDetail(getCurrentCustomerId().longValue(),
                orderId.longValue());
        if (order == null) {
            addActionError("That order could not be found for this customer.");
            return false;
        }
        return true;
    }

    public Collection<AssetType> getAssetTypeOptions() {
        return AssetType.values();
    }

    public Order getOrder() {
        return order;
    }

    public Asset getAsset() {
        return asset;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public String getAssetTypeCode() {
        return assetTypeCode;
    }

    public void setAssetTypeCode(final String assetTypeCode) {
        this.assetTypeCode = assetTypeCode;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(final String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(final String technicianName) {
        this.technicianName = technicianName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }
}
