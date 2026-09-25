package org.example.am.internal.web.action;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.shared.domain.OrderType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Collection;

/**
 * Starts a new order.
 */
@Component("OrderNewAction")
@Scope("prototype")
public class OrderNewAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    /** Sends a replacement order to the asset picker before the contact step. */
    private static final String RESULT_SELECT_ASSET = "selectAsset";

    private String orderTypeCode;


    public String initOrder() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        if (orderTypeCode != null) {
            model.setOrderType(OrderType.lookup(orderTypeCode));
        }
        storeModel(model);
        return Action.SUCCESS;
    }

    /**
     * Records the chosen order type and moves on.
     *
     * <p>A separate action from {@link #initOrder()}, which only renders the page. The type has to
     * be captured by something before the contact step, and the contact step is not the place to
     * know about order types.</p>
     *
     * <p>Returns {@code selectAsset} for the types that act on existing hardware, because those
     * cannot go straight to the contacts: the rest of the flow needs to know which asset is being
     * replaced, and a replacement order with no asset is rejected later anyway.</p>
     */
    public String selectOrderType() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        final OrderType orderType = OrderType.lookup(orderTypeCode);
        if (orderType == null) {
            addFieldErrorAndLog("orderTypeCode", "Choose an order type.");
            return Action.INPUT;
        }
        model.setOrderType(orderType);
        model.reachStep(1);
        storeModel(model);
        return model.isReplacement() && model.getSelectedAsset() == null
                ? RESULT_SELECT_ASSET : Action.SUCCESS;
    }

    /**
     * The order types the radio group on {@code orderNew.jsp} offers.
     *
     * <p>Read straight off the typesafe enum rather than stored on the model: the list is the same
     * for every user and every request, and {@code values()} already returns declaration order,
     * which is the order the screen should show them in.</p>
     */
    public Collection<OrderType> getOrderTypeOptions() {
        return OrderType.values();
    }

    public String getOrderTypeCode() {
        return orderTypeCode;
    }

    public void setOrderTypeCode(final String orderTypeCode) {
        this.orderTypeCode = orderTypeCode;
    }
}
