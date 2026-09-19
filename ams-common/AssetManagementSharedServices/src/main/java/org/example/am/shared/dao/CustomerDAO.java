package org.example.am.shared.dao;

import java.util.List;

import org.example.am.shared.domain.AmsService;
import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.Customer;

/** Reads and writes {@code AMS_CUSTOMERS} and the collections hanging off it. */
public interface CustomerDAO {

    Customer getCustomer(long customerId);

    List<Contact> getContactsForCustomer(long customerId);

    List<AmsService> getServicesForCustomer(long customerId);

    /**
     * Toggles the administration flag that gates the ordering flow.
     *
     * @return the number of rows updated, which is zero when the customer does not exist
     */
    int updateCanSubmitOrders(long customerId, boolean canSubmitOrders, String userId);

    boolean isEarlyAdopter(long customerId);
}
