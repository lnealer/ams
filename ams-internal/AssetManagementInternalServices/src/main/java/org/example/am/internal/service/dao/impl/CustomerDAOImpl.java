package org.example.am.internal.service.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.am.internal.service.dao.CustomerDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("internalCustomerDAO")
public class CustomerDAOImpl extends BaseDAO implements CustomerDAO {

    private static final String SELECT_RECENT =
            "SELECT * FROM ( SELECT C.CUSTOMER_ID, C.CUSTOMER_NAME, C.CUSTOMER_SHORT_NAME,"
          + "                       C.ACCOUNT_NUMBER, C.CAN_SUBMIT_ORDERS_FL, C.ACTIVE_FL "
          + "                  FROM AMS_CUSTOMER_VISITS V "
          + "                  JOIN AMS_CUSTOMERS C ON C.CUSTOMER_ID = V.CUSTOMER_ID "
          + "                 WHERE V.USER_ID = :userId "
          + "                 ORDER BY V.VISITED_DT DESC ) WHERE ROWNUM <= :maxRows ";

    private static final String DELETE_VISIT =
            "DELETE FROM AMS_CUSTOMER_VISITS WHERE USER_ID = :userId AND CUSTOMER_ID = :customerId ";

    private static final String INSERT_VISIT =
            "INSERT INTO AMS_CUSTOMER_VISITS ( USER_ID, CUSTOMER_ID, VISITED_DT ) "
          + "VALUES ( :userId, :customerId, SYSTIMESTAMP ) ";

    private static final RowMapper<Customer> CUSTOMER_MAPPER = new RowMapper<Customer>() {

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
    };

    @Override
    public List<Customer> getRecentCustomers(final String userId, final int maxRows) {
        return getNamedParameterJdbcTemplate().query(SELECT_RECENT,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_MAX_ROWS, Integer.valueOf(maxRows <= 0 ? 10 : maxRows))
                        .build(), CUSTOMER_MAPPER);
    }

    /**
     * Delete then insert, so revisiting a customer moves it to the top of the list rather than
     * leaving a stale timestamp behind.
     */
    @Override
    public void recordCustomerVisit(final String userId, final long customerId) {
        final ParameterRepository key = ParameterRepository.create()
                .with(CommonConstants.PARAM_USER_ID, userId)
                .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId));
        getNamedParameterJdbcTemplate().update(DELETE_VISIT, key.build());
        getNamedParameterJdbcTemplate().update(INSERT_VISIT, key.build());
    }
}
