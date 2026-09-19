package org.example.am.internal.web.action;

import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.CalendarService;
import org.example.am.internal.web.model.DecommissionModel;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.Action;

/**
 * Schedules and cancels the removal of an asset from service.
 */
@Component("DecommissionAction")
@Scope("prototype")
public class DecommissionAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private final DecommissionModel model = new DecommissionModel();

    @Autowired
    private transient AssetService assetService;

    @Autowired
    private transient CalendarService internalCalendarService;

    @Override
    public DecommissionModel getModel() {
        return model;
    }

    /** Renders the scheduling form. */
    public String initDecommission() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_DECOMMISSION_ASSET);
        if (denied != null) {
            return denied;
        }
        final Asset asset = loadAsset();
        if (asset == null) {
            return Action.ERROR;
        }
        // Re-checked here rather than trusted from the link the user clicked: the asset may have
        // picked up an order or a change request since the page that offered the link was drawn.
        if (!asset.isCanDecommission()) {
            addActionError("This asset cannot be decommissioned at the moment."
                    + " It has other work in flight against it.");
            return Action.ERROR;
        }
        model.setAssetTag(asset.getDisplayTag());
        model.setLatestSchedulableDate(internalCalendarService.getLatestDecommissionDate());
        return Action.SUCCESS;
    }

    /** Books the decommission date. */
    public String submitDecommission() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SCHEDULE_DECOMMISSION);
        if (denied != null) {
            return denied;
        }
        if (!model.isComplete()) {
            addActionError("Choose a date and give a reason.");
            return Action.INPUT;
        }
        final Asset asset = loadAsset();
        if (asset == null || !asset.isCanDecommission()) {
            addActionError("This asset can no longer be decommissioned.");
            return Action.ERROR;
        }

        try {
            final boolean reserved = internalCalendarService.reserveDecommissionDate(
                    model.getDecommissionId() == null ? 0L : model.getDecommissionId().longValue(),
                    model.getAssetId().longValue(), model.getScheduledDate(),
                    model.isHardwareReturnRequired(), getUserId());
            if (!reserved) {
                addActionError("That date is no longer available. Please choose another.");
                return Action.INPUT;
            }
        } catch (final IllegalArgumentException outsideWindow) {
            // The service enforces the scheduling window; surface its reason rather than a
            // generic failure, because the user can act on it.
            addFieldErrorAndLog("scheduledDate", outsideWindow.getMessage());
            model.setLatestSchedulableDate(internalCalendarService.getLatestDecommissionDate());
            return Action.INPUT;
        }

        addActionMessage("The decommission of " + model.getAssetTag() + " has been scheduled.");
        return Action.SUCCESS;
    }

    /** Releases a booked decommission date. */
    public String cancelDecommission() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CANCEL_DECOMMISSION);
        if (denied != null) {
            return denied;
        }
        if (model.getDecommissionId() == null) {
            addActionError("No decommission was identified.");
            return Action.ERROR;
        }
        final boolean cancelled = internalCalendarService.cancelDecommissionDate(
                model.getDecommissionId().longValue(), model.getReason(), getUserId());
        if (!cancelled) {
            addActionError("That decommission could not be cancelled.");
            return Action.ERROR;
        }
        addActionMessage("The scheduled decommission has been cancelled.");
        return Action.SUCCESS;
    }

    private Asset loadAsset() {
        if (model.getAssetId() == null) {
            addActionError("No asset was identified.");
            return null;
        }
        final long customerId = model.getCustomerId() == null
                ? (getCurrentCustomerId() == null ? 0L : getCurrentCustomerId().longValue())
                : model.getCustomerId().longValue();
        final Asset asset = assetService.getAssetDetail(customerId, model.getAssetId().longValue());
        if (asset == null) {
            addActionError("That asset could not be found for this customer.");
        }
        return asset;
    }
}
