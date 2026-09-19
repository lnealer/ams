package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.CustomerSearchDAO;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("customerSearchSharedDAO")
public class CustomerSearchDAOImpl extends BaseDAO implements CustomerSearchDAO {

    private static final String SEARCH_BY_NAME =
            "SELECT * FROM ( SELECT C.CUSTOMER_ID, C.CUSTOMER_NAME, C.CUSTOMER_SHORT_NAME,"
          + "                       C.ACCOUNT_NUMBER, C.CAN_SUBMIT_ORDERS_FL, C.ACTIVE_FL "
          + "                  FROM AMS_CUSTOMERS C "
          + "                 WHERE UPPER(C.CUSTOMER_NAME) LIKE :term ESCAPE '\\' "
          + "                 ORDER BY C.CUSTOMER_NAME ) WHERE ROWNUM <= :maxRows ";

    private static final String SEARCH_BY_ACCOUNT_NUMBER =
            "SELECT * FROM ( SELECT C.CUSTOMER_ID, C.CUSTOMER_NAME, C.CUSTOMER_SHORT_NAME,"
          + "                       C.ACCOUNT_NUMBER, C.CAN_SUBMIT_ORDERS_FL, C.ACTIVE_FL "
          + "                  FROM AMS_CUSTOMERS C "
          + "                 WHERE C.ACCOUNT_NUMBER = :accountNumber "
          + "                 ORDER BY C.CUSTOMER_NAME ) WHERE ROWNUM <= :maxRows ";

    private static final String LIST_CUSTOMERS =
            "SELECT * FROM ( SELECT C.CUSTOMER_ID, C.CUSTOMER_NAME, C.CUSTOMER_SHORT_NAME,"
          + "                       C.ACCOUNT_NUMBER, C.CAN_SUBMIT_ORDERS_FL, C.ACTIVE_FL "
          + "                  FROM AMS_CUSTOMERS C "
          + "                 ORDER BY C.CUSTOMER_NAME ) WHERE ROWNUM <= :maxRows ";

    private static final RowMapper<Customer> CUSTOMER_SUMMARY_MAPPER = new CustomerSummaryMapper();

    @Override
    public List<Customer> searchByName(final String term, final int maxRows) {
        if (term == null || term.trim().length() == 0) {
            return new ArrayList<Customer>();
        }
        return getNamedParameterJdbcTemplate().query(SEARCH_BY_NAME,
                ParameterRepository.create()
                        .withLikeTerm("term", term)
                        .with(CommonConstants.PARAM_MAX_ROWS, Integer.valueOf(cap(maxRows)))
                        .build(), CUSTOMER_SUMMARY_MAPPER);
    }

    @Override
    public List<Customer> searchByAccountNumber(final String accountNumber, final int maxRows) {
        if (accountNumber == null || accountNumber.trim().length() == 0) {
            return new ArrayList<Customer>();
        }
        return getNamedParameterJdbcTemplate().query(SEARCH_BY_ACCOUNT_NUMBER,
                ParameterRepository.create()
                        .with("accountNumber", accountNumber.trim())
                        .with(CommonConstants.PARAM_MAX_ROWS, Integer.valueOf(cap(maxRows)))
                        .build(), CUSTOMER_SUMMARY_MAPPER);
    }

    @Override
    public List<Customer> listCustomers(final int maxRows) {
        return getNamedParameterJdbcTemplate().query(LIST_CUSTOMERS,
                ParameterRepository.of(CommonConstants.PARAM_MAX_ROWS, Integer.valueOf(cap(maxRows)))
                        .build(), CUSTOMER_SUMMARY_MAPPER);
    }

    private static int cap(final int maxRows) {
        return maxRows <= 0 ? CommonConstants.MAX_SEARCH_RESULTS : maxRows;
    }

    /** Identity only: the search grid never needs the assets or services collections. */
    private static class CustomerSummaryMapper implements RowMapper<Customer> {

        @Override
        public Customer mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Customer customer = new Customer();
            customer.setCustomerId(ConversionUtils.getLong(rs, "CUSTOMER_ID"));
            customer.setCustomerName(ConversionUtils.getString(rs, "CUSTOMER_NAME"));
            customer.setCustomerShortName(ConversionUtils.getString(rs, "CUSTOMER_SHORT_NAME"));
            customer.setAccountNumber(ConversionUtils.getString(rs, "ACCOUNT_NUMBER"));
            customer.setCanSubmitOrders(ConversionUtils.getBoolean(rs, "CAN_SUBMIT_ORDERS_FL"));
            customer.setActive(ConversionUtils.getBoolean(rs, "ACTIVE_FL"));
            return customer;
        }
    }
}
