package org.example.am.internal.web.action;

import org.example.am.internal.utils.InternalConstants;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.shared.domain.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Shared behaviour for the steps of the ordering flow.
 *
 * <p>The flow spans several screens, so the partly completed order lives in the session between
 * them. Holding it there rather than persisting each step means an abandoned order leaves nothing
 * behind, at the cost of the model having to be found and stored on every request - which is what
 * {@link #getModel()} and {@link #storeModel()} are for.</p>
 */
public abstract class OrderBaseAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    @Autowired
    private transient org.example.am.shared.service.CustomerService customerService;

    /**
     * @return the in-progress order from the session, creating an empty one on first entry
     */
    @Override
    public OrderModel getModel() {
        final javax.servlet.http.HttpSession session = getOrCreateSession();
        OrderModel model = (OrderModel) session.getAttribute(InternalConstants.SESSION_ORDER_MODEL);
        if (model == null) {
            model = new OrderModel();
            model.setCustomerId(getCurrentCustomerId());
            session.setAttribute(InternalConstants.SESSION_ORDER_MODEL, model);
        }
        return model;
    }

    protected void storeModel(final OrderModel model) {
        getOrCreateSession().setAttribute(InternalConstants.SESSION_ORDER_MODEL, model);
    }

    /**
     * Drops the in-progress order. Called after a submit or an explicit cancel, so that returning
     * to the ordering screens starts cleanly rather than resuming a finished order.
     */
    protected void clearModel() {
        final javax.servlet.http.HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(InternalConstants.SESSION_ORDER_MODEL);
        }
    }

    protected org.example.am.shared.service.CustomerService getCustomerService() {
        return customerService;
    }

    /**
     * Checks the shipping address will fit on a carrier label.
     *
     * <p>The limits are the carrier's, not ours: a longer line is silently truncated by their label
     * printer, and the package goes to a subtly wrong address. Catching it here, where the user can
     * still shorten it, is the only place it can be fixed.</p>
     *
     * @return {@code true} when the address is acceptable
     */
    protected boolean validateAddressLengths(final Address address) {
        if (address == null) {
            addActionError("A shipping address is required.");
            return false;
        }
        boolean valid = true;
        if (exceeds(address.getAddressLine1(), Address.MAX_ADDRESS_LINE_LENGTH)) {
            addFieldErrorAndLog("shippingAddress.addressLine1",
                    "The first address line must be " + Address.MAX_ADDRESS_LINE_LENGTH
                    + " characters or fewer to fit the shipping label.");
            valid = false;
        }
        if (exceeds(address.getAddressLine2(), Address.MAX_ADDRESS_LINE_LENGTH)) {
            addFieldErrorAndLog("shippingAddress.addressLine2",
                    "The second address line must be " + Address.MAX_ADDRESS_LINE_LENGTH
                    + " characters or fewer to fit the shipping label.");
            valid = false;
        }
        if (exceeds(address.getCity(), Address.MAX_CITY_LENGTH)) {
            addFieldErrorAndLog("shippingAddress.city",
                    "The city must be " + Address.MAX_CITY_LENGTH
                    + " characters or fewer to fit the shipping label.");
            valid = false;
        }
        return valid;
    }

    private static boolean exceeds(final String value, final int limit) {
        return value != null && value.trim().length() > limit;
    }

    /**
     * Re-checks that the customer may still raise orders.
     *
     * <p>Called at the start of each step rather than only at the beginning: an operator can leave
     * a half-finished order open for a long time, and ordering may have been switched off for that
     * customer in the meantime.</p>
     */
    protected boolean validateCustomerCanOrder(final OrderModel model) {
        if (model.getCustomerId() == null) {
            addActionError("Choose a customer before starting an order.");
            return false;
        }
        if (!customerService.isOrderingEnabled(model.getCustomerId().longValue())) {
            addActionError("Ordering is not enabled for this customer.");
            return false;
        }
        return true;
    }

    /**
     * @return {@code true} when a user is still authenticated; guards the steps that would
     *         otherwise carry on with a half-populated session
     */
    protected boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }
}
