package org.example.am.internal.web.action;

import java.util.Date;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.domain.Order;
import org.example.am.shared.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The page shown after a successful submit.
 */
@Component("OrderConfirmationAction")
@Scope("prototype")
public class OrderConfirmationAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Order order;
    private Date earliestInstallationDate;

    /** Set by the redirect from the place step when the chosen despatch window had been taken. */
    private boolean shippingWindowLost;

    @Autowired
    private transient OrderService orderService;

    public String confirmation() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_ORDER);
        if (denied != null) {
            return denied;
        }
        if (orderId == null) {
            return Action.ERROR;
        }
        final Long customerId = getCurrentCustomerId();
        // The detail read, not the plain one: the confirmation is the receipt for everything that
        // was keyed, so it needs the contacts, the window, the configuration and the machine list.
        order = orderService.getOrderDetail(customerId == null ? 0L : customerId.longValue(),
                orderId.longValue());
        if (order == null) {
            addActionError("That order could not be found.");
            return Action.ERROR;
        }
        order.setCurrentTime(getCurrentTime());
        earliestInstallationDate = orderService.getEarliestInstallationDate(order);
        return Action.SUCCESS;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(final Order order) {
        this.order = order;
    }

    public boolean isShippingWindowLost() {
        return shippingWindowLost;
    }

    public void setShippingWindowLost(final boolean shippingWindowLost) {
        this.shippingWindowLost = shippingWindowLost;
    }

    public Date getEarliestInstallationDate() {
        return earliestInstallationDate;
    }

    public void setEarliestInstallationDate(final Date earliestInstallationDate) {
        this.earliestInstallationDate = earliestInstallationDate;
    }
}
