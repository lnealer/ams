package org.example.am.internal.service.dao;

import java.util.List;

import org.example.am.shared.domain.Customer;

/** Internal-only customer reads for the operator screens. */
public interface CustomerDAO {

    /** @return the customers an operator has recently acted on */
    List<Customer> getRecentCustomers(String userId, int maxRows);

    void recordCustomerVisit(String userId, long customerId);
}
