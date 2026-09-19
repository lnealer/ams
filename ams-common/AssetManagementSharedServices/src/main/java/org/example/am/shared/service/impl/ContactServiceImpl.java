package org.example.am.shared.service.impl;

import java.util.List;

import org.example.am.shared.dao.ContactDAO;
import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.ContactType;
import org.example.am.shared.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** The people attached to a customer's orders, assets and change requests. */
@Service("contactService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactDAO contactDAO;

    @Override
    public Contact getContact(final long contactId) {
        return contactDAO.getContact(contactId);
    }

    @Override
    public List<Contact> getActiveContacts(final long customerId, final ContactType contactType) {
        return contactDAO.getActiveContacts(customerId, contactType);
    }

    public void setContactDAO(final ContactDAO contactDAO) {
        this.contactDAO = contactDAO;
    }
}
