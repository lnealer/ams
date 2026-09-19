package org.example.am.internal.service;

import java.util.List;

import org.example.am.shared.domain.Customer;

/** Internal-only customer reads: the operator's recently visited list. */
public interface CustomerService {

    List<Customer> getRecentCustomers(String userId, int maxRows);

    /** Records that this operator looked at this customer, moving it to the top of their list. */
    void recordVisit(String userId, long customerId);
}
