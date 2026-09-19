package org.example.am.shared.domain;

import java.io.Serializable;

/**
 * A named human attached to an order, asset or change request.
 *
 * <p>Contacts are customer scoped: the same person may appear more than once with different
 * {@link ContactType} roles, which is why the role lives on the contact rather than on the
 * owning entity.</p>
 */
public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long contactId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private String phoneExtension;
    private String mobileNumber;
    private ContactType contactType;
    private Long customerId;
    private boolean active;

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(final Long contactId) {
        this.contactId = contactId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(final String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(final String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public void setContactType(final ContactType contactType) {
        this.contactType = contactType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * @return "Last, First", or whichever half is populated; never {@code null}.
     */
    public String getFullName() {
        final StringBuilder builder = new StringBuilder();
        if (lastName != null && lastName.trim().length() > 0) {
            builder.append(lastName.trim());
        }
        if (firstName != null && firstName.trim().length() > 0) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(firstName.trim());
        }
        return builder.toString();
    }

    /**
     * @return the phone number with the extension appended, as printed on the order confirmation.
     */
    public String getFormattedPhoneNumber() {
        if (phoneNumber == null) {
            return "";
        }
        if (phoneExtension == null || phoneExtension.trim().length() == 0) {
            return phoneNumber;
        }
        return phoneNumber + " x" + phoneExtension.trim();
    }

    @Override
    public String toString() {
        return getFullName();
    }

}
