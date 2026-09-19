package org.example.am.shared.service;

import java.util.List;

import org.example.am.shared.domain.AmsService;
import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.domain.ContactType;

/** Customer reads and the one administrative write the internal application performs. */
public interface CustomerService {

    Customer getCustomer(long customerId);

    List<Contact> getActiveContacts(long customerId, ContactType contactType);

    List<AmsService> getServices(long customerId);

    /** @return {@code true} when the flag was changed */
    boolean updateCanSubmitOrders(long customerId, boolean canSubmitOrders, String userId);

    boolean isOrderingEnabled(long customerId);
}
