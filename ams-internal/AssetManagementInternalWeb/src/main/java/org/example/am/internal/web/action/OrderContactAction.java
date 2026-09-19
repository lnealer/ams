package org.example.am.internal.web.action;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.ContactType;
import org.example.am.shared.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.Action;

/**
 * Step 1 of the ordering flow: who to talk to.
 *
 * <p>Three roles, and they are genuinely different people often enough to be worth asking for
 * separately: the person raising the order, the person who will sign for the box, and the person
 * the engineer rings from the car park. The shipping and installation contacts can each be copied
 * from the ordering one, which is what most orders do.</p>
 */
@Component("OrderContactAction")
@Scope("prototype")
public class OrderContactAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    /**
     * Deliberately permissive. This is only here to catch a transposed address before the
     * confirmation email bounces, not to adjudicate RFC 5321 - a validator strict enough to be
     * "correct" rejects addresses that work.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+\\.[^@\\s]+$");

    /** Digits, spaces and the usual punctuation; length is checked separately. */
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-. ]{7,30}$");

    @Autowired
    private transient ContactService contactService;

    /** Copies the ordering contact into the shipping one when the user ticks the box. */
    private boolean shippingSameAsOrdering;
    private boolean installationSameAsShipping;

    /** Renders the contact form. */
    public String initContacts() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        prepareBlankContacts(model);
        model.reachStep(1);
        storeModel(model);
        return Action.SUCCESS;
    }

    /** Validates the contacts and moves on to the address. */
    public String saveContacts() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        prepareBlankContacts(model);
        applyCopyBoxes(model);

        boolean valid = validateContact(model.getOrderingContact(), "orderingContact",
                "ordering contact");
        valid &= validateContact(model.getShippingContact(), "shippingContact", "shipping contact");
        valid &= validateContact(model.getInstallationContact(), "installationContact",
                "installation contact");
        if (!valid) {
            return Action.INPUT;
        }

        stampTypes(model);
        model.reachStep(2);
        storeModel(model);
        return Action.SUCCESS;
    }

    /**
     * The contacts already on file for this customer, offered so a repeat order does not have to
     * re-key someone the system already knows.
     */
    public List<Contact> getExistingContacts() {
        final Long customerId = getCurrentCustomerId();
        if (customerId == null) {
            return java.util.Collections.<Contact>emptyList();
        }
        return contactService.getActiveContacts(customerId.longValue(), null);
    }

    public Collection<ContactType> getContactTypeOptions() {
        return ContactType.values();
    }

    /**
     * Struts binds onto {@code orderingContact.firstName}, which needs the object to exist first -
     * OGNL will not create it on a nested property of a model it did not construct.
     */
    private static void prepareBlankContacts(final OrderModel model) {
        if (model.getOrderingContact() == null) {
            model.setOrderingContact(new Contact());
        }
        if (model.getShippingContact() == null) {
            model.setShippingContact(new Contact());
        }
        if (model.getInstallationContact() == null) {
            model.setInstallationContact(new Contact());
        }
    }

    /**
     * A copied contact is a copy, not a shared reference: they are persisted as three rows with
     * three different roles, and sharing the object would give all three the same id and the same
     * role.
     */
    private void applyCopyBoxes(final OrderModel model) {
        if (shippingSameAsOrdering) {
            model.setShippingContact(copyOf(model.getOrderingContact()));
        }
        if (installationSameAsShipping) {
            model.setInstallationContact(copyOf(model.getShippingContact()));
        }
    }

    private static Contact copyOf(final Contact source) {
        final Contact copy = new Contact();
        if (source == null) {
            return copy;
        }
        copy.setFirstName(source.getFirstName());
        copy.setLastName(source.getLastName());
        copy.setEmailAddress(source.getEmailAddress());
        copy.setPhoneNumber(source.getPhoneNumber());
        copy.setPhoneExtension(source.getPhoneExtension());
        copy.setMobileNumber(source.getMobileNumber());
        // Deliberately not the id: a copy is a new row with its own role, not the same person
        // recorded twice.
        return copy;
    }

    private static void stampTypes(final OrderModel model) {
        model.getOrderingContact().setContactType(ContactType.ORDERING);
        model.getShippingContact().setContactType(ContactType.SHIPPING);
        model.getInstallationContact().setContactType(ContactType.INSTALLATION);
    }

    private boolean validateContact(final Contact contact, final String field, final String label) {
        if (contact == null) {
            addFieldErrorAndLog(field + ".lastName", "Give a name for the " + label + ".");
            return false;
        }
        boolean valid = true;
        if (isBlank(contact.getFirstName())) {
            addFieldErrorAndLog(field + ".firstName", "A first name is required for the " + label + ".");
            valid = false;
        }
        if (isBlank(contact.getLastName())) {
            addFieldErrorAndLog(field + ".lastName", "A last name is required for the " + label + ".");
            valid = false;
        }
        if (isBlank(contact.getEmailAddress())) {
            addFieldErrorAndLog(field + ".emailAddress",
                    "An email address is required for the " + label + ".");
            valid = false;
        } else if (!EMAIL.matcher(contact.getEmailAddress().trim()).matches()) {
            addFieldErrorAndLog(field + ".emailAddress",
                    "That does not look like an email address.");
            valid = false;
        }
        if (isBlank(contact.getPhoneNumber())) {
            addFieldErrorAndLog(field + ".phoneNumber",
                    "A phone number is required for the " + label + ".");
            valid = false;
        } else if (!PHONE.matcher(contact.getPhoneNumber().trim()).matches()) {
            addFieldErrorAndLog(field + ".phoneNumber",
                    "A phone number may only contain digits, spaces and + ( ) - .");
            valid = false;
        }
        return valid;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().length() == 0;
    }

    public boolean isShippingSameAsOrdering() {
        return shippingSameAsOrdering;
    }

    public void setShippingSameAsOrdering(final boolean shippingSameAsOrdering) {
        this.shippingSameAsOrdering = shippingSameAsOrdering;
    }

    public boolean isInstallationSameAsShipping() {
        return installationSameAsShipping;
    }

    public void setInstallationSameAsShipping(final boolean installationSameAsShipping) {
        this.installationSameAsShipping = installationSameAsShipping;
    }
}
