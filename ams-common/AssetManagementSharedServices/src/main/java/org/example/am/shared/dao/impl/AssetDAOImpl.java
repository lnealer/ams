package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.am.shared.dao.AssetDAO;
import org.example.am.shared.domain.Address;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.AssetConfigurationStatusType;
import org.example.am.shared.domain.AssetStatusType;
import org.example.am.shared.domain.AssetType;
import org.example.am.shared.domain.CountryType;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.domain.Decommission;
import org.example.am.shared.domain.DecommissionStatusType;
import org.example.am.shared.domain.Order;
import org.example.am.shared.domain.OrderStatusType;
import org.example.am.shared.domain.OrderType;
import org.example.am.shared.domain.StateType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import org.example.am.shared.dao.BaseDAO;

/**
 * Hand written SQL against {@code AMS_ASSETS}, {@code AMS_ORDERS} and {@code AMS_ASSET_CONFIGS}.
 *
 * <p>Named {@code assetSharedDAO} because the internal application also has an {@code assetDAO} of
 * its own; the two coexist in one context once the shared library is on the internal classpath.</p>
 */
@Repository("assetSharedDAO")
public class AssetDAOImpl extends BaseDAO implements AssetDAO {

    private static final String ASSET_COLUMNS =
            "  A.ASSET_ID, A.CUSTOMER_ID, A.ASSET_TAG, A.LEGACY_ASSET_TAG, A.SERIAL_NUMBER,"
          + "  A.ASSET_TYPE_CD, A.ASSET_STATUS_CD, A.DATA_SOURCE_CD, A.MIGRATABLE_FL,"
          + "  A.EMERGENCY_REPL_FL, A.NEEDS_ATTENTION_FL, A.ATTENTION_REASON,"
          + "  A.CREATED_DT, A.MODIFIED_DT ";

    private static final String SELECT_ASSET =
            "SELECT " + ASSET_COLUMNS
          + "  FROM AMS_ASSETS A "
          + " WHERE A.CUSTOMER_ID = :customerId "
          + "   AND A.ASSET_ID = :assetId ";

    private static final String SELECT_ASSET_BY_TAG =
            "SELECT " + ASSET_COLUMNS
          + "  FROM AMS_ASSETS A "
          + " WHERE A.CUSTOMER_ID = :customerId "
          + "   AND UPPER(A.ASSET_TAG) = UPPER(:assetTag) ";

    private static final String SELECT_ASSET_BY_SERIAL =
            "SELECT " + ASSET_COLUMNS
          + "  FROM AMS_ASSETS A "
          + " WHERE UPPER(A.SERIAL_NUMBER) = UPPER(:serialNumber) ";

    private static final String SELECT_ASSETS_FOR_CUSTOMER =
            "SELECT " + ASSET_COLUMNS
          + "  FROM AMS_ASSETS A "
          + " WHERE A.CUSTOMER_ID = :customerId "
          + " ORDER BY A.ASSET_TAG ";

    private static final String SELECT_ASSET_STATUS =
            "SELECT A.ASSET_STATUS_CD "
          + "  FROM AMS_ASSETS A "
          + " WHERE A.CUSTOMER_ID = :customerId "
          + "   AND A.ASSET_ID = :assetId ";

    /**
     * The most recent order for the asset. Ordered by submitted date descending with the id as the
     * tie break, because two orders raised in the same second are not otherwise separable.
     */
    private static final String SELECT_ORDER_FOR_ASSET =
            "SELECT O.ORDER_ID, O.ORDER_NUMBER, O.ORDER_TYPE_CD, O.ORDER_STATUS_CD,"
          + "       O.CUSTOMER_ID, O.TRACKING_NUMBER, O.SUBMITTED_DT, O.SHIPPED_DT,"
          + "       O.REQUESTED_INSTALL_DT, O.CANCELLED_DT, O.CANCELLATION_REASON,"
          + "       O.CREATED_DT, O.MODIFIED_DT "
          + "  FROM AMS_ORDERS O "
          + " WHERE O.CUSTOMER_ID = :customerId "
          + "   AND O.ASSET_ID = :assetId "
          + " ORDER BY O.SUBMITTED_DT DESC, O.ORDER_ID DESC ";

    /**
     * Assets that a migration order may be raised against. The blocking conditions are evaluated in
     * SQL rather than in Java so that a customer with several thousand assets does not have to be
     * loaded in full just to populate a drop-down.
     */
    private static final String SELECT_MIGRATABLE_ASSETS =
            "SELECT " + ASSET_COLUMNS
          + "  FROM AMS_ASSETS A "
          + " WHERE A.CUSTOMER_ID = :customerId "
          + "   AND A.MIGRATABLE_FL = 'Y' "
          + "   AND A.ASSET_STATUS_CD IN ('INSTALLED', 'ACTIVE') "
          + "   AND NOT EXISTS ( SELECT 1 FROM AMS_ORDERS O "
          + "                     WHERE O.ASSET_ID = A.ASSET_ID "
          + "                       AND O.ORDER_STATUS_CD NOT IN ('COMPLETED', 'CANCELLED') ) "
          + "   AND NOT EXISTS ( SELECT 1 FROM AMS_NETWORK_CHANGE_REQUESTS N "
          + "                     WHERE N.ASSET_ID = A.ASSET_ID "
          + "                       AND N.NCR_STATUS_CD NOT IN ('COMPLETED', 'CANCELLED') ) "
          + "   AND NOT EXISTS ( SELECT 1 FROM AMS_DECOMMISSIONS D "
          + "                     WHERE D.ASSET_ID = A.ASSET_ID "
          + "                       AND D.DECOM_STATUS_CD IN ('REQUESTED', 'SCHEDULED') ) "
          + "   AND NOT EXISTS ( SELECT 1 FROM AMS_ASSET_CONFIGS C "
          + "                     WHERE C.ASSET_ID = A.ASSET_ID "
          + "                       AND C.CONFIG_STATUS_CD = 'MISMATCH' ) "
          + " ORDER BY A.ASSET_TAG ";

    private static final String SELECT_DECOMMISSION =
            "SELECT D.DECOMMISSION_ID, D.ASSET_ID, D.DECOM_STATUS_CD, D.REQUESTED_DT,"
          + "       D.SCHEDULED_DT, D.COMPLETED_DT, D.REASON, D.HARDWARE_RETURN_FL "
          + "  FROM AMS_DECOMMISSIONS D "
          + "  JOIN AMS_ASSETS A ON A.ASSET_ID = D.ASSET_ID "
          + " WHERE A.CUSTOMER_ID = :customerId "
          + "   AND D.ASSET_ID = :assetId "
          + " ORDER BY D.REQUESTED_DT DESC, D.DECOMMISSION_ID DESC ";

    private static final String NEXT_ASSET_RETURN_ID =
            "SELECT " + CommonConstants.SEQ_ASSET_RETURNS + ".NEXTVAL FROM DUAL ";

    private static final String INSERT_ASSET_RETURN =
            "INSERT INTO AMS_ASSET_RETURNS "
          + "       ( ASSET_RETURN_ID, ASSET_ID, SERIAL_NUMBER, RETURN_STATUS_CD,"
          + "         CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :assetReturnId, :assetId, :serialNumber, 'PENDING',"
          + "         SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String NEXT_ASSET_ID =
            "SELECT " + CommonConstants.SEQ_ASSETS + ".NEXTVAL FROM DUAL ";

    /**
     * DATA_SOURCE_CD is 'AMS' rather than 'LEGACY' or 'EXTFEED': this asset was created here, by
     * this application, from an order it can point at - which is what the column is for.
     */
    private static final String INSERT_ASSET =
            "INSERT INTO AMS_ASSETS "
          + "       ( ASSET_ID, CUSTOMER_ID, ASSET_TAG, SERIAL_NUMBER, ASSET_TYPE_CD,"
          + "         ASSET_STATUS_CD, DATA_SOURCE_CD, INSTALL_ADDRESS_ID,"
          + "         MIGRATABLE_FL, EMERGENCY_REPL_FL, NEEDS_ATTENTION_FL,"
          + "         CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :assetId, :customerId, :assetTag, :serialNumber, :assetTypeCode,"
          + "         :statusCode, 'AMS', :installAddressId,"
          + "         'N', 'N', 'N',"
          + "         SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String UPDATE_ASSET_STATUS =
            "UPDATE AMS_ASSETS "
          + "   SET ASSET_STATUS_CD = :statusCode,"
          + "       MODIFIED_DT = SYSTIMESTAMP,"
          + "       MODIFIED_BY = :userId "
          + " WHERE CUSTOMER_ID = :customerId "
          + "   AND ASSET_ID = :assetId ";

    private static final String UPDATE_INSTALL_ADDRESS =
            "UPDATE AMS_ASSETS "
          + "   SET INSTALL_ADDRESS_ID = :addressId,"
          + "       MODIFIED_DT = SYSTIMESTAMP,"
          + "       MODIFIED_BY = :userId "
          + " WHERE CUSTOMER_ID = :customerId "
          + "   AND ASSET_ID = :assetId ";

    private static final RowMapper<Asset> ASSET_DETAIL_MAPPER = new AssetDetailMapper();
    private static final RowMapper<Asset> ASSET_MIGRATABLE_MAPPER = new AssetMigratableMapper();
    private static final RowMapper<Order> ORDER_MAPPER = new AssetOrderMapper();
    private static final RowMapper<Decommission> DECOMMISSION_MAPPER = new DecommissionMapper();

    @Override
    public long insertAsset(final Asset asset, final long customerId, final String userId) {
        final Long assetId = getNamedParameterJdbcTemplate().queryForObject(NEXT_ASSET_ID,
                ParameterRepository.create().build(), Long.class);

        // Tag and serial are derived from the same id, so they cannot drift apart or collide, and
        // the tag reads the way every other tag in the system does.
        final String tag = String.format("AMS-%06d", assetId);
        final String serial = "SN-" + assetId;
        asset.setAssetId(assetId);
        asset.setAssetTag(tag);
        asset.setSerialNumber(serial);

        getNamedParameterJdbcTemplate().update(INSERT_ASSET, ParameterRepository.create()
                .with(CommonConstants.PARAM_ASSET_ID, assetId)
                .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                .with("assetTag", tag)
                .with("serialNumber", serial)
                .with("assetTypeCode", asset.getAssetType() == null ? null : asset.getAssetType().getCode())
                .with("statusCode", asset.getAssetStatusType() == null
                        ? null : asset.getAssetStatusType().getCode())
                .with("installAddressId", asset.getInstallationAddress() == null
                        ? null : asset.getInstallationAddress().getAddressId())
                .with(CommonConstants.PARAM_USER_ID, userId)
                .build());
        logger.info("Created asset {} ({}) for customer {}", assetId, tag, Long.valueOf(customerId));
        return assetId.longValue();
    }

    @Override
    public Asset getAsset(final long customerId, final long assetId) {
        return firstOrNull(getNamedParameterJdbcTemplate().query(SELECT_ASSET,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                        .build(),
                ASSET_DETAIL_MAPPER));
    }

    @Override
    public Asset getAssetByAssetTag(final long customerId, final String assetTag) {
        if (assetTag == null || assetTag.trim().length() == 0) {
            return null;
        }
        return firstOrNull(getNamedParameterJdbcTemplate().query(SELECT_ASSET_BY_TAG,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .with("assetTag", assetTag.trim())
                        .build(),
                ASSET_DETAIL_MAPPER));
    }

    @Override
    public Asset getAssetBySerialNumber(final String serialNumber) {
        if (serialNumber == null || serialNumber.trim().length() == 0) {
            return null;
        }
        return firstOrNull(getNamedParameterJdbcTemplate().query(SELECT_ASSET_BY_SERIAL,
                ParameterRepository.of("serialNumber", serialNumber.trim()).build(),
                ASSET_DETAIL_MAPPER));
    }

    @Override
    public Order getOrderForAsset(final long customerId, final long assetId) {
        return firstOrNull(getNamedParameterJdbcTemplate().query(SELECT_ORDER_FOR_ASSET,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                        .build(),
                ORDER_MAPPER));
    }

    @Override
    public AssetStatusType getAssetStatus(final long customerId, final long assetId) {
        try {
            final String code = getNamedParameterJdbcTemplate().queryForObject(SELECT_ASSET_STATUS,
                    ParameterRepository.create()
                            .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                            .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                            .build(),
                    String.class);
            return AssetStatusType.lookup(code);
        } catch (final EmptyResultDataAccessException noSuchAsset) {
            logger.debug("No asset {} for customer {}", Long.valueOf(assetId),
                    Long.valueOf(customerId));
            return null;
        }
    }

    @Override
    public List<Asset> getAssetsForCustomer(final long customerId) {
        return getNamedParameterJdbcTemplate().query(SELECT_ASSETS_FOR_CUSTOMER,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .build(),
                ASSET_DETAIL_MAPPER);
    }

    @Override
    public List<Asset> getMigratableAssetsForCustomer(final Customer customer) {
        if (customer == null || customer.getCustomerId() == null) {
            return new ArrayList<Asset>();
        }
        final List<Asset> assets = getNamedParameterJdbcTemplate().query(SELECT_MIGRATABLE_ASSETS,
                ParameterRepository.of(CommonConstants.PARAM_CUSTOMER_ID, customer.getCustomerId())
                        .build(),
                ASSET_MIGRATABLE_MAPPER);
        for (final Asset asset : assets) {
            asset.setCustomer(customer);
        }
        return assets;
    }

    @Override
    public Decommission getDecommissionForAsset(final long customerId, final long assetId) {
        return firstOrNull(getNamedParameterJdbcTemplate().query(SELECT_DECOMMISSION,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                        .build(),
                DECOMMISSION_MAPPER));
    }

    @Override
    public long addAssetReturn(final long assetId, final String serialNumber, final String userId) {
        final Long assetReturnId = getNamedParameterJdbcTemplate().queryForObject(
                NEXT_ASSET_RETURN_ID, ParameterRepository.create().build(), Long.class);
        getNamedParameterJdbcTemplate().update(INSERT_ASSET_RETURN,
                ParameterRepository.create()
                        .with("assetReturnId", assetReturnId)
                        .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                        .with("serialNumber", serialNumber)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .build());
        logger.info("Recorded asset return {} for asset {}", assetReturnId, Long.valueOf(assetId));
        return assetReturnId.longValue();
    }

    @Override
    public int updateAssetStatus(final long customerId, final long assetId,
            final AssetStatusType status, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_ASSET_STATUS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_STATUS_CODE, status == null ? null : status.getCode())
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                        .build());
    }

    @Override
    public int updateInstallationAddress(final long customerId, final long assetId,
            final long addressId, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_INSTALL_ADDRESS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_ADDRESS_ID, Long.valueOf(addressId))
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_CUSTOMER_ID, Long.valueOf(customerId))
                        .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                        .build());
    }

    private static <T> T firstOrNull(final List<T> rows) {
        return rows.isEmpty() ? null : rows.get(0);
    }

    // ------------------------------------------------------------------
    // Row mappers
    // ------------------------------------------------------------------

    /**
     * Maps the common asset projection. Nested objects are left null: the service layer decides
     * which of them are worth a second query for the screen being rendered.
     */
    private static class AssetDetailMapper implements RowMapper<Asset> {

        @Override
        public Asset mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Asset asset = new Asset();
            asset.setAssetId(ConversionUtils.getLong(rs, "ASSET_ID"));
            asset.setAssetTag(ConversionUtils.getString(rs, "ASSET_TAG"));
            asset.setLegacyAssetTag(ConversionUtils.getString(rs, "LEGACY_ASSET_TAG"));
            asset.setSerialNumber(ConversionUtils.getString(rs, "SERIAL_NUMBER"));
            asset.setAssetType(AssetType.lookup(ConversionUtils.getString(rs, "ASSET_TYPE_CD")));
            asset.setAssetStatusType(
                    AssetStatusType.lookup(ConversionUtils.getString(rs, "ASSET_STATUS_CD")));
            asset.setMigratable(ConversionUtils.getBoolean(rs, "MIGRATABLE_FL"));
            asset.setEmergencyReplacementEnabled(ConversionUtils.getBoolean(rs, "EMERGENCY_REPL_FL"));
            asset.setNeedsAttention(ConversionUtils.getBoolean(rs, "NEEDS_ATTENTION_FL"));
            asset.setAttentionReason(ConversionUtils.getString(rs, "ATTENTION_REASON"));
            asset.setCreatedDate(ConversionUtils.getDate(rs, "CREATED_DT"));
            asset.setModifiedDate(ConversionUtils.getDate(rs, "MODIFIED_DT"));
            return asset;
        }
    }

    /**
     * Same projection as {@link AssetDetailMapper}, but the SQL has already proved the asset is
     * clear to migrate, so the flag is set without another round trip.
     */
    private static class AssetMigratableMapper extends AssetDetailMapper {

        @Override
        public Asset mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Asset asset = super.mapRow(rs, rowNumber);
            asset.setMigratable(true);
            return asset;
        }
    }

    private static class AssetOrderMapper implements RowMapper<Order> {

        @Override
        public Order mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Order order = new Order();
            order.setOrderId(ConversionUtils.getLong(rs, "ORDER_ID"));
            order.setOrderNumber(ConversionUtils.getString(rs, "ORDER_NUMBER"));
            order.setOrderType(OrderType.lookup(ConversionUtils.getString(rs, "ORDER_TYPE_CD")));
            order.setOrderStatusType(
                    OrderStatusType.lookup(ConversionUtils.getString(rs, "ORDER_STATUS_CD")));
            order.setCustomerId(ConversionUtils.getLong(rs, "CUSTOMER_ID"));
            order.setTrackingNumber(ConversionUtils.getString(rs, "TRACKING_NUMBER"));
            order.setSubmittedDate(ConversionUtils.getDate(rs, "SUBMITTED_DT"));
            order.setShippedDate(ConversionUtils.getDate(rs, "SHIPPED_DT"));
            order.setRequestedInstallationDate(ConversionUtils.getDate(rs, "REQUESTED_INSTALL_DT"));
            order.setCancelledDate(ConversionUtils.getDate(rs, "CANCELLED_DT"));
            order.setCancellationReason(ConversionUtils.getString(rs, "CANCELLATION_REASON"));
            order.setCreatedDate(ConversionUtils.getDate(rs, "CREATED_DT"));
            order.setModifiedDate(ConversionUtils.getDate(rs, "MODIFIED_DT"));
            return order;
        }
    }

    private static class DecommissionMapper implements RowMapper<Decommission> {

        @Override
        public Decommission mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Decommission decommission = new Decommission();
            decommission.setDecommissionId(ConversionUtils.getLong(rs, "DECOMMISSION_ID"));
            decommission.setAssetId(ConversionUtils.getLong(rs, "ASSET_ID"));
            decommission.setDecommissionStatusType(
                    DecommissionStatusType.lookup(ConversionUtils.getString(rs, "DECOM_STATUS_CD")));
            decommission.setRequestedDate(ConversionUtils.getDate(rs, "REQUESTED_DT"));
            decommission.setScheduledDate(ConversionUtils.getDate(rs, "SCHEDULED_DT"));
            decommission.setCompletedDate(ConversionUtils.getDate(rs, "COMPLETED_DT"));
            decommission.setReason(ConversionUtils.getString(rs, "REASON"));
            decommission.setHardwareReturnRequired(
                    ConversionUtils.getBoolean(rs, "HARDWARE_RETURN_FL"));
            return decommission;
        }
    }

    /**
     * Retained for the compare-configuration screen, which needs the stored revision and its port
     * rows rather than the summary the asset screen shows.
     */
    static final class AssetConfigurationMapper implements RowMapper<AssetConfiguration> {

        @Override
        public AssetConfiguration mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final AssetConfiguration configuration = new AssetConfiguration();
            configuration.setConfigurationId(ConversionUtils.getLong(rs, "CONFIG_ID"));
            configuration.setAssetId(ConversionUtils.getLong(rs, "ASSET_ID"));
            configuration.setAssetConfigurationStatusType(AssetConfigurationStatusType.lookup(
                    ConversionUtils.getString(rs, "CONFIG_STATUS_CD")));
            configuration.setLanIpAddress(ConversionUtils.getString(rs, "LAN_IP_ADDRESS"));
            configuration.setLanSubnetMask(ConversionUtils.getString(rs, "LAN_SUBNET_MASK"));
            configuration.setWanIpAddress(ConversionUtils.getString(rs, "WAN_IP_ADDRESS"));
            configuration.setWanSubnetMask(ConversionUtils.getString(rs, "WAN_SUBNET_MASK"));
            configuration.setDefaultGateway(ConversionUtils.getString(rs, "DEFAULT_GATEWAY"));
            configuration.setEffectiveDate(ConversionUtils.getDate(rs, "EFFECTIVE_DT"));
            final Integer revision = ConversionUtils.getInteger(rs, "REVISION_NUM");
            configuration.setRevision(revision == null ? 0 : revision.intValue());
            return configuration;
        }
    }

    /**
     * Used by the update-install-address flow, which reads the address back to echo it on the
     * confirmation page.
     */
    static final class AddressMapper implements RowMapper<Address> {

        @Override
        public Address mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final Address address = new Address();
            address.setAddressId(ConversionUtils.getLong(rs, "ADDRESS_ID"));
            address.setAddressLine1(ConversionUtils.getString(rs, "ADDRESS_LINE_1"));
            address.setAddressLine2(ConversionUtils.getString(rs, "ADDRESS_LINE_2"));
            address.setCity(ConversionUtils.getString(rs, "CITY"));
            address.setZipCode(ConversionUtils.getString(rs, "ZIP_CODE"));
            address.setCounty(ConversionUtils.getString(rs, "COUNTY"));
            address.setState(StateType.lookup(ConversionUtils.getString(rs, "STATE_CD")));
            address.setCountry(CountryType.lookup(ConversionUtils.getString(rs, "COUNTRY_CD")));
            address.setValidated(ConversionUtils.getBoolean(rs, "VALIDATED_FL"));
            return address;
        }
    }
}
