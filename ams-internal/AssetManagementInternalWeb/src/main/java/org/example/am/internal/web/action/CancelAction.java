package org.example.am.internal.web.action;

import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.CancelModel;
import org.example.am.shared.domain.Order;
import org.example.am.shared.domain.PropertyType;
import org.example.am.shared.service.ModifyConfigurationService;
import org.example.am.shared.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.Action;

/**
 * Cancels an order or an in-flight configuration change.
 *
 * <p>Two steps by design. The first renders the consequences - above all whether the cancellation
 * charge applies - and the second performs it. The penalty is recalculated at submit time rather
 * than trusted from the form, because the answer depends on the clock and the user may have sat on
 * the confirmation page.</p>
 */
@Component("CancelAction")
@Scope("prototype")
public class CancelAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_MIN_HOURS_TO_CANCEL = 48;

    private final CancelModel model = new CancelModel();

    @Autowired
    private transient OrderService orderService;

    @Autowired
    private transient ModifyConfigurationService modifyConfigurationService;

    @Override
    public CancelModel getModel() {
        return model;
    }

    /** Renders the confirmation page for an order cancellation. */
    public String cancelOrder() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CANCEL_ORDER);
        if (denied != null) {
            return denied;
        }
        final Order order = loadOrder();
        if (order == null) {
            return Action.ERROR;
        }
        model.setOrderNumber(order.getOrderNumber());
        model.setScheduledInstallationDate(order.getScheduledInstallationDate());
        model.setMinimumHoursToCancelWithoutPenalty(getMinimumHoursToCancel());
        model.setPenaltyIncurred(
                order.isCancellationPenaltyIncurred(getMinimumHoursToCancel()));
        return Action.SUCCESS;
    }

    /** Performs the order cancellation. */
    public String submitCancelOrder() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CANCEL_ORDER);
        if (denied != null) {
            return denied;
        }
        if (model.getReason() == null || model.getReason().trim().length() == 0) {
            addFieldErrorAndLog("reason", "Give a reason for the cancellation.");
            return Action.INPUT;
        }
        final Order order = loadOrder();
        if (order == null) {
            return Action.ERROR;
        }

        // Recomputed rather than read back from the form: the user may have been on the
        // confirmation page long enough for the answer to have changed.
        model.setPenaltyIncurred(order.isCancellationPenaltyIncurred(getMinimumHoursToCancel()));

        final boolean cancelled = orderService.cancelOrder(getCustomerId(),
                model.getOrderId().longValue(), model.getReason().trim(), getUserId());
        if (!cancelled) {
            addActionError("That order has already been completed or cancelled.");
            return Action.INPUT;
        }
        addActionMessage("Order " + model.getOrderNumber() + " has been cancelled."
                + (model.isPenaltyIncurred() ? " The cancellation charge applies." : ""));
        return Action.SUCCESS;
    }

    /** Renders the confirmation page for cancelling a pending configuration change. */
    public String cancelModifyConfiguration() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_MODIFY_CONFIG);
        if (denied != null) {
            return denied;
        }
        if (model.getConfigurationId() == null) {
            addActionError("No configuration change was identified.");
            return Action.ERROR;
        }
        return Action.SUCCESS;
    }

    /** Performs the configuration change cancellation. */
    public String submitCancelModifyConfiguration() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_MODIFY_CONFIG);
        if (denied != null) {
            return denied;
        }
        if (model.getConfigurationId() == null || model.getAssetId() == null) {
            addActionError("No configuration change was identified.");
            return Action.ERROR;
        }
        modifyConfigurationService.reapplyStoredConfiguration(
                model.getAssetId().longValue(), getUserId());
        addActionMessage("The pending configuration change has been cancelled.");
        return Action.SUCCESS;
    }

    private Order loadOrder() {
        if (model.getOrderId() == null) {
            addActionError("No order was identified.");
            return null;
        }
        final Order order = orderService.getOrder(getCustomerId(), model.getOrderId().longValue());
        if (order == null) {
            addActionError("That order could not be found for this customer.");
            return null;
        }
        // Every rule below reads the clock through the order, so stamp the request's time onto it.
        order.setCurrentTime(getCurrentTime());
        return order;
    }

    private long getCustomerId() {
        if (model.getCustomerId() != null) {
            return model.getCustomerId().longValue();
        }
        final Long sessionCustomerId = getCurrentCustomerId();
        return sessionCustomerId == null ? 0L : sessionCustomerId.longValue();
    }

    private int getMinimumHoursToCancel() {
        return getConfigService().getInt(
                PropertyType.MIN_HOURS_BEFORE_INSTALLATION_TO_CANCEL_ORDER_WITHOUT_PENALTY,
                DEFAULT_MIN_HOURS_TO_CANCEL);
    }
}
