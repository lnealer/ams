package org.example.am.shared.service;

import java.util.List;

import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.ContactType;

/** The people attached to a customer's orders, assets and change requests. */
public interface ContactService {

    Contact getContact(long contactId);

    /**
     * @param contactType the role to filter on, or {@code null} for every active contact
     * @return the customer's active contacts
     */
    List<Contact> getActiveContacts(long customerId, ContactType contactType);
}
