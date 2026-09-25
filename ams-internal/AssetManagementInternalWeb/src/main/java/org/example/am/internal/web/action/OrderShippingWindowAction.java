package org.example.am.internal.web.action;

import java.util.Collections;
import java.util.List;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.shared.domain.Address;
import org.example.am.shared.domain.Timeslot;
import org.example.am.shared.service.ShippingCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Step 6, the last one: when the hardware is despatched.
 *
 * <p>The windows on offer come from the warehouse region serving the destination postcode, so this
 * step depends on the address having been keyed. A postcode nobody has mapped to a region yields
 * no windows; that is a gap in the map rather than an error, and the order can still be placed -
 * operations assign a window by hand afterwards.</p>
 *
 * <p>The window is not held while the user looks at it. It is reserved at the moment the order is
 * placed, which is the only point at which holding capacity is justified.</p>
 */
@Component("OrderShippingWindowAction")
@Scope("prototype")
public class OrderShippingWindowAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    @Autowired
    private transient ShippingCalendarService shippingCalendarService;

    private List<Timeslot> shippingWindows = Collections.<Timeslot>emptyList();
    private String shippingRegion;

    public String initShippingWindow() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        loadWindows(model);
        storeModel(model);
        return Action.SUCCESS;
    }

    /**
     * Records the chosen window and moves on to placing the order.
     *
     * <p>The choice is re-checked against the windows currently on offer rather than trusted from
     * the form, because the list is rendered from the database and a posted id that is not in it
     * is either a stale page or a hand-edited one.</p>
     */
    public String saveShippingWindow() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        loadWindows(model);

        final Long chosen = model.getShippingWindowTimeslotId();
        if (chosen == null) {
            addFieldErrorAndLog("shippingWindowTimeslotId", "Choose a despatch window.");
            return Action.INPUT;
        }
        final Timeslot window = findOffered(chosen);
        if (window == null) {
            model.setShippingWindowTimeslotId(null);
            addFieldErrorAndLog("shippingWindowTimeslotId",
                    "That despatch window is no longer available. Choose another.");
            return Action.INPUT;
        }
        model.setShippingWindow(window);
        model.reachStep(7);
        storeModel(model);
        return Action.SUCCESS;
    }

    /** Lets the user place the order without a window when none can be offered. */
    public String skipShippingWindow() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        model.setShippingWindow(null);
        model.setShippingWindowTimeslotId(null);
        model.reachStep(7);
        storeModel(model);
        return Action.SUCCESS;
    }

    public List<Timeslot> getShippingWindows() {
        return shippingWindows;
    }

    public String getShippingRegion() {
        return shippingRegion;
    }

    /** @return {@code true} when the destination postcode is not mapped to a warehouse region */
    public boolean isRegionUnmapped() {
        return shippingRegion == null;
    }

    private void loadWindows(final OrderModel model) {
        final Address address = model.getShippingAddress();
        final String zipCode = address == null ? null : address.getZipCode();
        shippingRegion = shippingCalendarService.getRegion(zipCode);
        shippingWindows = shippingCalendarService.getShippingWindowsForZipCode(zipCode);
        if (shippingRegion == null) {
            logger.info("No warehouse region for ZIP {}; despatch window cannot be offered", zipCode);
        }
    }

    private Timeslot findOffered(final Long timeslotId) {
        for (final Timeslot window : shippingWindows) {
            if (timeslotId.equals(window.getTimeslotId())) {
                return window;
            }
        }
        return null;
    }
}
