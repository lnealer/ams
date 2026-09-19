package org.example.am.shared.service;

import java.util.List;

import org.example.am.shared.domain.Customer;

/** Customer lookup for the search screen's customer picker. */
public interface CustomerSearchService {

    List<Customer> search(String term);

    List<Customer> search(String term, int maxRows);

    /** @return every customer, for the picker; there is nothing to search by on first use */
    List<Customer> listAll(int maxRows);
}
