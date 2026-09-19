package org.example.am.internal.web.action;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.shared.domain.Order;
import org.example.am.shared.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Places the order.
 *
 * <p>There is no review screen in front of this: the user presses "Place order" on the despatch
 * window step and the order is raised. That makes this the only place the whole model is checked,
 * so everything the six steps established is re-validated here rather than trusted - the model has
 * been sitting in a session that may have been open for a long time, and both the customer's
 * ordering permission and the asset's eligibility can have changed while it sat there.</p>
 */
@Component("OrderSubmitAction")
@Scope("prototype")
public class OrderSubmitAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    /**
     * @return the human facing order number. The database key is the identity; this is what appears
     *         on paperwork and is what people quote on the phone.
     */
    private static String generateOrderNumber() {
        return "ORD-" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();
    }

    private Long submittedOrderId;
    private String orderNumber;
    private boolean shippingWindowLost;

    @Autowired
    private transient OrderService orderService;

    public String submitOrder() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SUBMIT_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        if (!validateStepsComplete(model)) {
            return Action.INPUT;
        }
        if (!validateAddressLengths(model.getShippingAddress())) {
            return Action.INPUT;
        }
        if (!validateDueDiligence(model)) {
            return Action.INPUT;
        }

        final Order order = model.toOrder();
        order.setCurrentTime(getCurrentTime());
        if (order.getAssetProblemForMigratingOrder() != null) {
            addActionError("This order can no longer go ahead: "
                    + order.getAssetProblemForMigratingOrder().getDescription() + ".");
            return Action.INPUT;
        }
        if (order.getDueDiligence() != null && !order.getDueDiligence().isSatisfied()) {
            addActionError("Due diligence has not been completed or waived.");
            return Action.INPUT;
        }

        orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);
        submittedOrderId = Long.valueOf(orderService.submitOrder(order, getUserId()));

        // The window can fill between choosing it and pressing the button. The order stands, but
        // the confirmation has to say so - it reads from the database, where a lost window is
        // simply absent and looks like one that was never chosen. Carried on the redirect rather
        // than as an action message, which would not survive it.
        shippingWindowLost = order.getShippingWindowTimeslotId() == null
                && model.getShippingWindowTimeslotId() != null;

        // The session copy has served its purpose; leaving it would let a refresh raise a
        // second order.
        clearModel();
        return Action.SUCCESS;
    }

    /**
     * Catches an order placed by posting straight at this action without walking the steps.
     *
     * <p>Struts will happily route a hand-made POST here, and every one of these fields is
     * nullable in the database, so without this an order could be raised with no contacts, no
     * address and no configuration and nothing would complain until the warehouse tried to stage
     * it.</p>
     */
    private boolean validateStepsComplete(final OrderModel model) {
        if (model.getOrderType() == null) {
            addActionError("Start the order again: no order type was chosen.");
            return false;
        }
        if (model.getOrderingContact() == null || model.getShippingContact() == null
                || model.getInstallationContact() == null) {
            addActionError("Contact information is incomplete.");
            return false;
        }
        if (model.getShippingAddress() == null || !model.getShippingAddress().isComplete()) {
            addActionError("The shipping address is incomplete.");
            return false;
        }
        if (model.getDeviceNickname() == null || model.getDeviceNickname().trim().length() == 0) {
            addActionError("The device has no nickname.");
            return false;
        }
        if (model.getMaintenanceWindow() == null || model.getMaintenanceWindow().getDayType() == null) {
            addActionError("No maintenance window has been chosen.");
            return false;
        }
        // The status is the test, not the network type. initConfiguration() defaults the network
        // type to STATIC on arrival, so checking that only proved the operator had opened the
        // screen - an order whose configuration step was REJECTED still satisfied it and went on to
        // be placed, despatched, and staged from a configuration with no addresses in it.
        // saveConfiguration() stamps PENDING only after the WAN and LAN validators have passed, so
        // a status here means the step genuinely completed.
        if (model.getAssetConfiguration() == null
                || model.getAssetConfiguration().getNetworkConfigurationType() == null
                || model.getAssetConfiguration().getAssetConfigurationStatusType() == null) {
            addActionError("The external configuration is incomplete.");
            return false;
        }
        if (model.getPopulatedSubscriberPcs().isEmpty()) {
            addActionError("No subscriber machines have been listed.");
            return false;
        }
        return true;
    }

    private boolean validateDueDiligence(final OrderModel model) {
        if (model.isDueDiligenceCompleted()) {
            return true;
        }
        final boolean waived = model.getDueDiligenceWaiverReason() != null
                && model.getDueDiligenceWaiverReason().trim().length() > 0;
        if (!waived) {
            addFieldErrorAndLog("dueDiligenceCompleted",
                    "Confirm due diligence, or give a reason for waiving it.");
            return false;
        }
        // Waiving is a deliberate override, so it needs its own role.
        if (!hasRole(SecurityRoleType.INT_OVERRIDE_DUE_DILIGENCE)) {
            addFieldErrorAndLog("dueDiligenceWaiverReason",
                    "You are not permitted to waive due diligence.");
            return false;
        }
        return true;
    }

    public Long getSubmittedOrderId() {
        return submittedOrderId;
    }

    public void setSubmittedOrderId(final Long submittedOrderId) {
        this.submittedOrderId = submittedOrderId;
    }

    public boolean isShippingWindowLost() {
        return shippingWindowLost;
    }

    public void setShippingWindowLost(final boolean shippingWindowLost) {
        this.shippingWindowLost = shippingWindowLost;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(final String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
