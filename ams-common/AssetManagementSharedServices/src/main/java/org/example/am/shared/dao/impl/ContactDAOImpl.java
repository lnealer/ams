package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.ContactDAO;
import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.ContactType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("contactSharedDAO")
public class ContactDAOImpl extends BaseDAO implements ContactDAO {

    private static final String CONTACT_COLUMNS =
            "  T.CONTACT_ID, T.CUSTOMER_ID, T.FIRST_NAME, T.LAST_NAME, T.EMAIL_ADDRESS,"
          + "  T.PHONE_NUMBER, T.PHONE_EXTENSION, T.MOBILE_NUMBER, T.CONTACT_TYPE_CD, T.ACTIVE_FL ";

    private static final String SELECT_CONTACT =
            "SELECT " + CONTACT_COLUMNS + " FROM AMS_CONTACTS T WHERE T.CONTACT_ID = :contactId ";

    /**
     * The contact type predicate is optional: passing a null type returns every active contact,
     * which is what the contact picker needs.
     */
    private static final String SELECT_ACTIVE_CONTACTS =
            "SELECT " + CONTACT_COLUMNS
          + "  FROM AMS_CONTACTS T "
          + " WHERE T.CUSTOMER_ID = :customerId "
          + "   AND T.ACTIVE_FL = 'Y' "
          + "   AND ( :typeCode IS NULL OR T.CONTACT_TYPE_CD = :typeCode ) "
          + " ORDER BY T.LAST_NAME, T.FIRST_NAME ";

    private static final String NEXT_CONTACT_ID = "SELECT AMS_CONTACTS_SQ.NEXTVAL FROM DUAL ";

    private static final String INSERT_CONTACT =
            "INSERT INTO AMS_CONTACTS "
          + "       ( CONTACT_ID, CUSTOMER_ID, FIRST_NAME, LAST_NAME, EMAIL_ADDRESS, PHONE_NUMBER,"
          + "         PHONE_EXTENSION, MOBILE_NUMBER, CONTACT_TYPE_CD, ACTIVE_FL,"
          + "         CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :contactId, :customerId, :firstName, :lastName, :emailAddress, :phoneNumber,"
          + "         :phoneExtension, :mobileNumber, :typeCode, :activeFlag,"
          + "         SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String UPDATE_CONTACT =
            "UPDATE AMS_CONTACTS "
          + "   SET FIRST_NAME = :firstName, LAST_NAME = :lastName, EMAIL_ADDRESS = :emailAddress,"
          + "       PHONE_NUMBER = :phoneNumber, PHONE_EXTENSION = :phoneExtension,"
          + "       MOBILE_NUMBER = :mobileNumber, CONTACT_TYPE_CD = :typeCode,"
          + "       ACTIVE_FL = :activeFlag, MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE CONTACT_ID = :contactId ";

    private static final String DEACTIVATE_CONTACT =
            "UPDATE AMS_CONTACTS "
          + "   SET ACTIVE_FL = 'N', MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE CONTACT_ID = :contactId ";

    private static final RowMapper<Contact> CONTACT_MAPPER = new ContactMapper();

    @Override
    public Contact getContact(final long contactId) {
        final List<Contact> rows = getNamedParameterJdbcTemplate().query(SELECT_CONTACT,
                ParameterRepository.of(CommonConstants.PARAM_CONTACT_ID, Long.valueOf(contactId))
                        .build(), CONTACT_MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<Contact> getActiveContacts(final long customerId, final ContactType contactType) {
        return getNamedParameterJdbcTemplate().query(SELECT_ACTIVE_CONTACTS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .with(CommonConstants.PARAM_TYPE_CODE,
                                contactType == null ? null : contactType.getCode())
                        .build(), CONTACT_MAPPER);
    }

    @Override
    public long insertContact(final Contact contact, final String userId) {
        final Long contactId = getNamedParameterJdbcTemplate().queryForObject(NEXT_CONTACT_ID,
                ParameterRepository.create().build(), Long.class);
        getNamedParameterJdbcTemplate().update(INSERT_CONTACT,
                parameters(contact, userId)
                        .with(CommonConstants.PARAM_CONTACT_ID, contactId)
                        .with(CommonConstants.PARAM_CUSTOMER_ID, contact.getCustomerId())
                        .build());
        contact.setContactId(contactId);
        return contactId.longValue();
    }

    @Override
    public int updateContact(final Contact contact, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_CONTACT,
                parameters(contact, userId)
                        .with(CommonConstants.PARAM_CONTACT_ID, contact.getContactId()).build());
    }

    @Override
    public int deactivateContact(final long contactId, final String userId) {
        return getNamedParameterJdbcTemplate().update(DEACTIVATE_CONTACT,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_CONTACT_ID, Long.valueOf(contactId))
                        .build());
    }

    private static ParameterRepository parameters(final Contact contact, final String userId) {
        return ParameterRepository.create()
                .with("firstName", contact.getFirstName())
                .with("lastName", contact.getLastName())
                .with("emailAddress", contact.getEmailAddress())
                .with("phoneNumber", contact.getPhoneNumber())
                .with("phoneExtension", contact.getPhoneExtension())
                .with("mobileNumber", contact.getMobileNumber())
                .with(CommonConstants.PARAM_TYPE_CODE,
                        contact.getContactType() == null ? null : contact.getContactType().getCode())
                .withFlag("activeFlag", contact.isActive())
                .with(CommonConstants.PARAM_USER_ID, userId);
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
}
