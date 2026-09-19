package org.example.am.shared.dao;

import java.util.List;

import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.ContactType;

/** Reads and writes {@code AMS_CONTACTS}. */
public interface ContactDAO {

    Contact getContact(long contactId);

    List<Contact> getActiveContacts(long customerId, ContactType contactType);

    long insertContact(Contact contact, String userId);

    int updateContact(Contact contact, String userId);

    int deactivateContact(long contactId, String userId);
}
