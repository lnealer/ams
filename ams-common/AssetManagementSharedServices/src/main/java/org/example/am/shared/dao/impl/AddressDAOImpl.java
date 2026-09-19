package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.am.shared.dao.AddressDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.Address;
import org.example.am.shared.domain.AddressType;
import org.example.am.shared.domain.CountryType;
import org.example.am.shared.domain.StateType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("addressSharedDAO")
public class AddressDAOImpl extends BaseDAO implements AddressDAO {

    private static final String ADDRESS_COLUMNS =
            "  D.ADDRESS_ID, D.ADDRESS_TYPE_CD, D.ADDRESS_LINE_1, D.ADDRESS_LINE_2, D.CITY,"
          + "  D.ZIP_CODE, D.COUNTY, D.STATE_CD, D.COUNTRY_CD, D.ATTENTION_TO, D.VALIDATED_FL ";

    private static final String SELECT_ADDRESS =
            "SELECT " + ADDRESS_COLUMNS + " FROM AMS_ADDRESSES D WHERE D.ADDRESS_ID = :addressId ";

    private static final String NEXT_ADDRESS_ID = "SELECT AMS_ADDRESSES_SQ.NEXTVAL FROM DUAL ";

    private static final String INSERT_ADDRESS =
            "INSERT INTO AMS_ADDRESSES "
          + "       ( ADDRESS_ID, ADDRESS_TYPE_CD, ADDRESS_LINE_1, ADDRESS_LINE_2, CITY, ZIP_CODE,"
          + "         COUNTY, STATE_CD, COUNTRY_CD, ATTENTION_TO, VALIDATED_FL,"
          + "         CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :addressId, :addressTypeCode, :addressLine1, :addressLine2, :city, :zipCode,"
          + "         :county, :stateCode, :countryCode, :attentionTo, :validatedFlag,"
          + "         SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String UPDATE_ADDRESS =
            "UPDATE AMS_ADDRESSES "
          + "   SET ADDRESS_LINE_1 = :addressLine1, ADDRESS_LINE_2 = :addressLine2, CITY = :city,"
          + "       ZIP_CODE = :zipCode, COUNTY = :county, STATE_CD = :stateCode,"
          + "       COUNTRY_CD = :countryCode, ATTENTION_TO = :attentionTo,"
          + "       VALIDATED_FL = :validatedFlag, MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE ADDRESS_ID = :addressId ";

    private static final String MARK_VALIDATED =
            "UPDATE AMS_ADDRESSES "
          + "   SET VALIDATED_FL = :validatedFlag, MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE ADDRESS_ID = :addressId ";

    private static final RowMapper<Address> ADDRESS_MAPPER = new AddressMapper();

    @Override
    public Address getAddress(final long addressId) {
        final List<Address> rows = getNamedParameterJdbcTemplate().query(SELECT_ADDRESS,
                ParameterRepository.of(CommonConstants.PARAM_ADDRESS_ID, Long.valueOf(addressId))
                        .build(), ADDRESS_MAPPER);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public long insertAddress(final Address address, final String userId) {
        final Long addressId = getNamedParameterJdbcTemplate().queryForObject(NEXT_ADDRESS_ID,
                ParameterRepository.create().build(), Long.class);
        getNamedParameterJdbcTemplate().update(INSERT_ADDRESS,
                parameters(address, userId).with(CommonConstants.PARAM_ADDRESS_ID, addressId)
                        .with("addressTypeCode", address.getAddressType() == null
                                ? null : address.getAddressType().getCode())
                        .build());
        address.setAddressId(addressId);
        return addressId.longValue();
    }

    @Override
    public int updateAddress(final Address address, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_ADDRESS,
                parameters(address, userId)
                        .with(CommonConstants.PARAM_ADDRESS_ID, address.getAddressId()).build());
    }

    @Override
    public int markValidated(final long addressId, final boolean validated, final String userId) {
        return getNamedParameterJdbcTemplate().update(MARK_VALIDATED,
                ParameterRepository.create()
                        .withFlag("validatedFlag", validated)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_ADDRESS_ID, Long.valueOf(addressId))
                        .build());
    }

    private static ParameterRepository parameters(final Address address, final String userId) {
        return ParameterRepository.create()
                .with("addressLine1", address.getAddressLine1())
                .with("addressLine2", address.getAddressLine2())
                .with("city", address.getCity())
                .with("zipCode", address.getZipCode())
                .with("county", address.getCounty())
                .with("stateCode", address.getState() == null ? null : address.getState().getCode())
                .with("countryCode", address.getCountry() == null ? null : address.getCountry().getCode())
                .with("attentionTo", address.getAttentionTo())
                .withFlag("validatedFlag", address.isValidated())
                .with(CommonConstants.PARAM_USER_ID, userId);
    }

    private static class AddressMapper implements RowMapper<Address> {

        @Override
        public Address mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Address address = new Address();
            address.setAddressId(ConversionUtils.getLong(rs, "ADDRESS_ID"));
            address.setAddressType(AddressType.lookup(ConversionUtils.getString(rs, "ADDRESS_TYPE_CD")));
            address.setAddressLine1(ConversionUtils.getString(rs, "ADDRESS_LINE_1"));
            address.setAddressLine2(ConversionUtils.getString(rs, "ADDRESS_LINE_2"));
            address.setCity(ConversionUtils.getString(rs, "CITY"));
            address.setZipCode(ConversionUtils.getString(rs, "ZIP_CODE"));
            address.setCounty(ConversionUtils.getString(rs, "COUNTY"));
            address.setState(StateType.lookup(ConversionUtils.getString(rs, "STATE_CD")));
            address.setCountry(CountryType.lookup(ConversionUtils.getString(rs, "COUNTRY_CD")));
            address.setAttentionTo(ConversionUtils.getString(rs, "ATTENTION_TO"));
            address.setValidated(ConversionUtils.getBoolean(rs, "VALIDATED_FL"));
            return address;
        }
    }
}
