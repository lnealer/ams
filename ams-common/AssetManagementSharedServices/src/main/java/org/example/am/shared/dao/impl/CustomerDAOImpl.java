package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.CustomerDAO;
import org.example.am.shared.domain.AmsService;
import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.ContactType;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.domain.CustomerEarlyAdopter;
import org.example.am.shared.domain.ServiceStatusType;
import org.example.am.shared.domain.ServiceType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("customerSharedDAO")
public class CustomerDAOImpl extends BaseDAO implements CustomerDAO {

    private static final String SELECT_CUSTOMER =
            "SELECT C.CUSTOMER_ID, C.CUSTOMER_NAME, C.CUSTOMER_SHORT_NAME, C.ACCOUNT_NUMBER,"
          + "       C.CAN_SUBMIT_ORDERS_FL, C.ACTIVE_FL,"
          + "       E.EARLY_ADOPTER_FL, E.ENROLLED_DT, E.PROGRAM_NAME "
          + "  FROM AMS_CUSTOMERS C "
          + "  LEFT JOIN AMS_CUSTOMER_EARLY_ADOPTERS E ON E.CUSTOMER_ID = C.CUSTOMER_ID "
          + " WHERE C.CUSTOMER_ID = :customerId ";

    private static final String SELECT_CONTACTS =
            "SELECT T.CONTACT_ID, T.CUSTOMER_ID, T.FIRST_NAME, T.LAST_NAME, T.EMAIL_ADDRESS,"
          + "       T.PHONE_NUMBER, T.PHONE_EXTENSION, T.MOBILE_NUMBER, T.CONTACT_TYPE_CD, T.ACTIVE_FL "
          + "  FROM AMS_CONTACTS T "
          + " WHERE T.CUSTOMER_ID = :customerId "
          + " ORDER BY T.LAST_NAME, T.FIRST_NAME ";

    private static final String SELECT_SERVICES =
            "SELECT S.SERVICE_ID, S.CUSTOMER_ID, S.SERVICE_TYPE_CD, S.SERVICE_STATUS_CD,"
          + "       S.START_DT, S.END_DT, S.DESCRIPTION, S.EXTERNAL_SERVICE_REF "
          + "  FROM AMS_SERVICES S "
          + " WHERE S.CUSTOMER_ID = :customerId "
          + " ORDER BY S.SERVICE_TYPE_CD ";

    private static final String UPDATE_CAN_SUBMIT_ORDERS =
            "UPDATE AMS_CUSTOMERS "
          + "   SET CAN_SUBMIT_ORDERS_FL = :canSubmitOrders,"
          + "       MODIFIED_DT = SYSTIMESTAMP,"
          + "       MODIFIED_BY = :userId "
          + " WHERE CUSTOMER_ID = :customerId ";

    private static final String SELECT_EARLY_ADOPTER_COUNT =
            "SELECT COUNT(*) FROM AMS_CUSTOMER_EARLY_ADOPTERS E "
          + " WHERE E.CUSTOMER_ID = :customerId AND E.EARLY_ADOPTER_FL = 'Y' ";

    private static final RowMapper<Customer> CUSTOMER_MAPPER = new CustomerMapper();
    private static final RowMapper<Contact> CONTACT_MAPPER = new ContactMapper();
    private static final RowMapper<AmsService> SERVICE_MAPPER = new ServiceMapper();

    @Override
    public Customer getCustomer(final long customerId) {
        final List<Customer> rows = getNamedParameterJdbcTemplate().query(SELECT_CUSTOMER,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build(), CUSTOMER_MAPPER);
        if (rows.isEmpty()) {
            return null;
        }
        final Customer customer = rows.get(0);
        customer.setContacts(getContactsForCustomer(customerId));
        customer.setServices(getServicesForCustomer(customerId));
        return customer;
    }

    @Override
    public List<Contact> getContactsForCustomer(final long customerId) {
        return getNamedParameterJdbcTemplate().query(SELECT_CONTACTS,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build(), CONTACT_MAPPER);
    }

    @Override
    public List<AmsService> getServicesForCustomer(final long customerId) {
        return getNamedParameterJdbcTemplate().query(SELECT_SERVICES,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build(), SERVICE_MAPPER);
    }

    @Override
    public int updateCanSubmitOrders(final long customerId, final boolean canSubmitOrders,
            final String userId) {
        final int updated = getNamedParameterJdbcTemplate().update(UPDATE_CAN_SUBMIT_ORDERS,
                ParameterRepository.create()
                        .withFlag("canSubmitOrders", canSubmitOrders)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build());
        logger.info("Ordering {} for customer {} by {}", canSubmitOrders ? "enabled" : "disabled",
                Long.valueOf(customerId), userId);
        return updated;
    }

    @Override
    public boolean isEarlyAdopter(final long customerId) {
        final Integer count = getNamedParameterJdbcTemplate().queryForObject(
                SELECT_EARLY_ADOPTER_COUNT,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build(), Integer.class);
        return count != null && count.intValue() > 0;
    }

    private static class CustomerMapper implements RowMapper<Customer> {

        @Override
        public Customer mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Customer customer = new Customer();
            customer.setCustomerId(ConversionUtils.getLong(rs, "CUSTOMER_ID"));
            customer.setCustomerName(ConversionUtils.getString(rs, "CUSTOMER_NAME"));
            customer.setCustomerShortName(ConversionUtils.getString(rs, "CUSTOMER_SHORT_NAME"));
            customer.setAccountNumber(ConversionUtils.getString(rs, "ACCOUNT_NUMBER"));
            customer.setCanSubmitOrders(ConversionUtils.getBoolean(rs, "CAN_SUBMIT_ORDERS_FL"));
            customer.setActive(ConversionUtils.getBoolean(rs, "ACTIVE_FL"));

            // The outer join means the early adopter columns are only present for enrolled customers.
            if (ConversionUtils.getString(rs, "EARLY_ADOPTER_FL") != null) {
                final CustomerEarlyAdopter earlyAdopter = new CustomerEarlyAdopter();
                earlyAdopter.setCustomerId(customer.getCustomerId());
                earlyAdopter.setEarlyAdopter(ConversionUtils.getBoolean(rs, "EARLY_ADOPTER_FL"));
                earlyAdopter.setEnrolledDate(ConversionUtils.getDate(rs, "ENROLLED_DT"));
                earlyAdopter.setProgramName(ConversionUtils.getString(rs, "PROGRAM_NAME"));
                customer.setCustomerEarlyAdopter(earlyAdopter);
            }
            return customer;
        }
    }

    private static class ContactMapper implements RowMapper<Contact> {

        @Override
        public Contact mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Contact contact = new Contact();
            contact.setContactId(ConversionUtils.getLong(rs, "CONTACT_ID"));
            contact.setCustomerId(ConversionUtils.getLong(rs, "CUSTOMER_ID"));
            contact.setFirstName(ConversionUtils.getString(rs, "FIRST_NAME"));
            contact.setLastName(ConversionUtils.getString(rs, "LAST_NAME"));
            contact.setEmailAddress(ConversionUtils.getString(rs, "EMAIL_ADDRESS"));
            contact.setPhoneNumber(ConversionUtils.getString(rs, "PHONE_NUMBER"));
            contact.setPhoneExtension(ConversionUtils.getString(rs, "PHONE_EXTENSION"));
            contact.setMobileNumber(ConversionUtils.getString(rs, "MOBILE_NUMBER"));
            contact.setContactType(ContactType.lookup(ConversionUtils.getString(rs, "CONTACT_TYPE_CD")));
            contact.setActive(ConversionUtils.getBoolean(rs, "ACTIVE_FL"));
            return contact;
        }
    }

    private static class ServiceMapper implements RowMapper<AmsService> {

        @Override
        public AmsService mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final AmsService service = new AmsService();
            service.setServiceId(ConversionUtils.getLong(rs, "SERVICE_ID"));
            service.setCustomerId(ConversionUtils.getLong(rs, "CUSTOMER_ID"));
            service.setServiceType(ServiceType.lookup(ConversionUtils.getString(rs, "SERVICE_TYPE_CD")));
            service.setServiceStatusType(
                    ServiceStatusType.lookup(ConversionUtils.getString(rs, "SERVICE_STATUS_CD")));
            service.setStartDate(ConversionUtils.getDate(rs, "START_DT"));
            service.setEndDate(ConversionUtils.getDate(rs, "END_DT"));
            service.setDescription(ConversionUtils.getString(rs, "DESCRIPTION"));
            service.setExternalServiceReference(
                    ConversionUtils.getString(rs, "EXTERNAL_SERVICE_REF"));
            return service;
        }
    }
}
