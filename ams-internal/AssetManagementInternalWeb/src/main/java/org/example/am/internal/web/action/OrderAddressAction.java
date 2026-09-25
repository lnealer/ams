package org.example.am.internal.web.action;

import java.util.Collection;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.shared.domain.Address;
import org.example.am.shared.domain.CountryType;
import org.example.am.shared.domain.StateType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Step 2 of the ordering flow: where the hardware goes.
 *
 * <p>One address serves as both the shipping destination and the installation site, because for
 * the sites this system serves they are the same place - the box is delivered to the shop and
 * installed in the shop. Splitting them would be two forms to fill in identically.</p>
 *
 * <p>The address validation interceptor wraps {@link #saveAddress()}, so the result it returns
 * ({@code av.success}, {@code av.suggestion} or {@code av.error}) decides whether the user moves
 * straight on, is shown a correction to confirm, or is told the check could not be made. A
 * validation outage does not stop the order.</p>
 */
@Component("OrderAddressAction")
@Scope("prototype")
public class OrderAddressAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    /** Renders the address form. */
    public String initAddress() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        if (model.getShippingAddress() == null) {
            model.setShippingAddress(new Address());
        }
        if (model.getShippingAddress().getCountry() == null) {
            model.getShippingAddress().setCountry(CountryType.US);
        }
        storeModel(model);
        return Action.SUCCESS;
    }

    /**
     * Validates what was keyed, then hands over to the address validation interceptor.
     *
     * <p>Anything the interceptor might improve is checked here first. Sending an obviously
     * incomplete address to the validation service wastes a call and comes back as a "could not
     * verify", which reads to the user as a service problem rather than as a missing postcode.</p>
     */
    public String saveAddress() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        final Address address = model.getShippingAddress();
        if (!validateAddressLengths(address)) {
            return Action.INPUT;
        }
        if (!validateRequiredParts(address)) {
            return Action.INPUT;
        }

        // Re-keying the address invalidates any previous verdict on it: a suggestion accepted for
        // the old text says nothing about the new.
        if (!model.isAddressSuggestionAccepted()) {
            address.setValidated(false);
        }
        model.reachStep(3);
        storeModel(model);
        return Action.SUCCESS;
    }

    /** Takes the correction the validation service offered in place of what was keyed. */
    public String acceptSuggestion() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (model.getSuggestedAddress() == null) {
            addActionError("There is no suggested address to accept.");
            return Action.INPUT;
        }
        model.setShippingAddress(model.getSuggestedAddress());
        model.setSuggestedAddress(null);
        model.setAddressSuggestionAccepted(true);
        model.reachStep(3);
        storeModel(model);
        return Action.SUCCESS;
    }

    /**
     * Keeps the address exactly as keyed and carries on.
     *
     * <p>Offered on both the suggestion and the unverified pages, because the validation service
     * is wrong often enough - new builds, recently renumbered streets - that "no, I meant what I
     * typed" has to be a supported answer rather than a dead end.</p>
     */
    public String keepAddressAsKeyed() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        model.setSuggestedAddress(null);
        // Not marked validated: it was not. The order carries VALIDATED_FL = 'N' and the warehouse
        // can see that nobody confirmed this address.
        model.reachStep(3);
        storeModel(model);
        return Action.SUCCESS;
    }

    public Collection<StateType> getStateOptions() {
        return StateType.values();
    }

    public Collection<CountryType> getCountryOptions() {
        return CountryType.values();
    }

    private boolean validateRequiredParts(final Address address) {
        boolean valid = true;
        if (isBlank(address.getAddressLine1())) {
            addFieldErrorAndLog("shippingAddress.addressLine1", "The first address line is required.");
            valid = false;
        }
        if (isBlank(address.getCity())) {
            addFieldErrorAndLog("shippingAddress.city", "The city is required.");
            valid = false;
        }
        if (address.getState() == null) {
            addFieldErrorAndLog("shippingAddress.state", "Choose a state.");
            valid = false;
        }
        if (isBlank(address.getZipCode())) {
            addFieldErrorAndLog("shippingAddress.zipCode", "The ZIP code is required.");
            valid = false;
        }
        return valid;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().length() == 0;
    }
}
