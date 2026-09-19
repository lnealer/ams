package org.example.am.shared.dao;

import java.util.List;

import org.example.am.shared.domain.Customer;

/** Type-ahead and full customer search used by the internal search screen. */
public interface CustomerSearchDAO {

    List<Customer> searchByName(String term, int maxRows);

    List<Customer> searchByAccountNumber(String accountNumber, int maxRows);

    /**
     * Every customer, for the picker an operator uses to choose who they are acting for.
     *
     * <p>Distinct from the search methods because the picker has to work before the operator knows
     * any search term - there is nothing to type on first use.</p>
     */
    List<Customer> listCustomers(int maxRows);
}
