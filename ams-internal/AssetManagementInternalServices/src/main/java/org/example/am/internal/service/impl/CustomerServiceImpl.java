package org.example.am.internal.service.impl;

import java.util.List;

import org.example.am.internal.service.CustomerService;
import org.example.am.internal.service.dao.CustomerDAO;
import org.example.am.shared.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("internalCustomerService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    /** How many customers the recently-visited list keeps. */
    private static final int DEFAULT_RECENT_COUNT = 10;

    @Autowired
    private CustomerDAO internalCustomerDAO;

    @Override
    public List<Customer> getRecentCustomers(final String userId, final int maxRows) {
        return internalCustomerDAO.getRecentCustomers(userId,
                maxRows <= 0 ? DEFAULT_RECENT_COUNT : maxRows);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void recordVisit(final String userId, final long customerId) {
        internalCustomerDAO.recordCustomerVisit(userId, customerId);
    }
}
