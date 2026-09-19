package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.am.shared.dao.AssetConfigDAO;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.PortConfiguration;
import org.example.am.shared.domain.PortConfigurationType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository("assetConfigSharedDAO")
public class AssetConfigDAOImpl extends BaseDAO implements AssetConfigDAO {

    private static final String CONFIG_COLUMNS =
            "  G.CONFIG_ID, G.ASSET_ID, G.CONFIG_TYPE_CD, G.CONFIG_STATUS_CD, G.NETWORK_CONFIG_CD,"
          + "  G.LAN_IP_ADDRESS, G.LAN_SUBNET_MASK, G.WAN_IP_ADDRESS, G.WAN_SUBNET_MASK,"
          + "  G.DEFAULT_GATEWAY, G.LAN_GATEWAY, G.PRIMARY_DNS, G.SECONDARY_DNS, G.CIRCUIT_ID, G.BANDWIDTH_KBPS,"
          + "  G.EFFECTIVE_DT, G.REVISION_NUM ";

    /**
     * The current revision is the highest numbered row that has not been superseded. Wrapped in an
     * inline view with {@code ROWNUM} rather than using an analytic function, because the same
     * statement has to run against the integration test database.
     */
    private static final String SELECT_CURRENT =
            "SELECT * FROM ( SELECT " + CONFIG_COLUMNS
          + "                  FROM AMS_ASSET_CONFIGS G "
          + "                 WHERE G.ASSET_ID = :assetId "
          + "                   AND G.CONFIG_STATUS_CD <> 'SUPERSEDE' "
          + "                 ORDER BY G.REVISION_NUM DESC ) WHERE ROWNUM <= 1 ";

    private static final String SELECT_ALL_FOR_CUSTOMER =
            "SELECT " + CONFIG_COLUMNS
          + "  FROM AMS_ASSET_CONFIGS G "
          + "  LEFT JOIN AMS_ASSETS A ON A.ASSET_ID = G.ASSET_ID "
          + "  LEFT JOIN AMS_ORDERS O ON O.ORDER_ID = G.ORDER_ID "
          + " WHERE ( A.CUSTOMER_ID = :customerId OR O.CUSTOMER_ID = :customerId ) "
          + "   AND G.CONFIG_STATUS_CD <> 'SUPERSEDE' "
          + " ORDER BY G.EFFECTIVE_DT DESC, G.CONFIG_ID DESC ";

    private static final String SELECT_WAN_ADDRESSES_IN_USE =
            "SELECT DISTINCT G.WAN_IP_ADDRESS FROM AMS_ASSET_CONFIGS G "
          + " WHERE G.WAN_IP_ADDRESS IS NOT NULL AND G.CONFIG_STATUS_CD <> 'SUPERSEDE' ";

    private static final String ATTACH_TO_ASSET =
            "UPDATE AMS_ASSET_CONFIGS "
          + "   SET ASSET_ID = :assetId, MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE CONFIG_ID = :configurationId AND ASSET_ID IS NULL ";

    private static final String MARK_APPLIED =
            "UPDATE AMS_ASSET_CONFIGS "
          + "   SET CONFIG_STATUS_CD = 'APPLIED', EFFECTIVE_DT = SYSTIMESTAMP,"
          + "       MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE CONFIG_ID = :configurationId AND CONFIG_STATUS_CD <> 'APPLIED' ";

    private static final String SELECT_HISTORY =
            "SELECT " + CONFIG_COLUMNS
          + "  FROM AMS_ASSET_CONFIGS G WHERE G.ASSET_ID = :assetId ORDER BY G.REVISION_NUM DESC ";

    private static final String SELECT_PORTS =
            "SELECT P.PORT_ID, P.CONFIG_ID, P.PORT_NAME, P.PORT_CONFIG_CD, P.MAC_ADDRESS,"
          + "       P.ACTIVE_FL, P.VLAN_ID, P.IP_ADDRESS, P.SUBNET_MASK "
          + "  FROM AMS_PORT_CONFIGS P WHERE P.CONFIG_ID = :configurationId ORDER BY P.PORT_NAME ";

    private static final String NEXT_CONFIG_ID = "SELECT AMS_ASSET_CONFIGS_SQ.NEXTVAL FROM DUAL ";

    private static final String NEXT_REVISION =
            "SELECT NVL(MAX(G.REVISION_NUM), 0) + 1 FROM AMS_ASSET_CONFIGS G WHERE G.ASSET_ID = :assetId ";

    private static final String INSERT_CONFIG =
            "INSERT INTO AMS_ASSET_CONFIGS "
          + "       ( CONFIG_ID, ASSET_ID, CONFIG_TYPE_CD, CONFIG_STATUS_CD, NETWORK_CONFIG_CD,"
          + "         LAN_IP_ADDRESS, LAN_SUBNET_MASK, WAN_IP_ADDRESS, WAN_SUBNET_MASK,"
          + "         DEFAULT_GATEWAY, LAN_GATEWAY, PRIMARY_DNS, SECONDARY_DNS, CIRCUIT_ID,"
          + "         BANDWIDTH_KBPS,"
          + "         EFFECTIVE_DT, REVISION_NUM, CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :configurationId, :assetId, :configTypeCode, :statusCode, :networkConfigCode,"
          + "         :lanIpAddress, :lanSubnetMask, :wanIpAddress, :wanSubnetMask,"
          + "         :defaultGateway, :lanGateway, :primaryDns, :secondaryDns, :circuitId,"
          + "         :bandwidthKbps,"
          + "         :effectiveDate, :revision, SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String SELECT_FOR_ORDER =
            "SELECT " + CONFIG_COLUMNS
          + "  FROM AMS_ASSET_CONFIGS G WHERE G.ORDER_ID = :orderId "
          + " ORDER BY G.REVISION_NUM DESC ";

    /**
     * Revision 1 always: an order's configuration is written once, when the order is placed, and
     * every later change is a modify-configuration against the installed asset. Numbering it off
     * MAX(REVISION_NUM) like the asset path does would have to scan on a null ASSET_ID.
     */
    private static final String INSERT_ORDER_CONFIG =
            "INSERT INTO AMS_ASSET_CONFIGS "
          + "       ( CONFIG_ID, ORDER_ID, ASSET_ID, CONFIG_TYPE_CD, CONFIG_STATUS_CD,"
          + "         NETWORK_CONFIG_CD, LAN_IP_ADDRESS, LAN_SUBNET_MASK, WAN_IP_ADDRESS,"
          + "         WAN_SUBNET_MASK, DEFAULT_GATEWAY, LAN_GATEWAY, PRIMARY_DNS, SECONDARY_DNS,"
          + "         CIRCUIT_ID,"
          + "         BANDWIDTH_KBPS, EFFECTIVE_DT, REVISION_NUM,"
          + "         CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :configurationId, :orderId, NULL, :configTypeCode, :statusCode,"
          + "         :networkConfigCode, :lanIpAddress, :lanSubnetMask, :wanIpAddress,"
          + "         :wanSubnetMask, :defaultGateway, :lanGateway, :primaryDns, :secondaryDns,"
          + "         :circuitId,"
          + "         :bandwidthKbps, :effectiveDate, 1,"
          + "         SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String MARK_SUPERSEDED =
            "UPDATE AMS_ASSET_CONFIGS "
          + "   SET CONFIG_STATUS_CD = 'SUPERSEDE', MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE CONFIG_ID = :configurationId ";

    private static final RowMapper<PortConfiguration> PORT_MAPPER = new PortMapper();

    @Override
    public AssetConfiguration getCurrentConfiguration(final long assetId) {
        final List<AssetConfiguration> rows = getNamedParameterJdbcTemplate().query(SELECT_CURRENT,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId)).build(),
                SharedRowMappers.ASSET_CONFIGURATION);
        if (rows.isEmpty()) {
            return null;
        }
        final AssetConfiguration configuration = rows.get(0);
        configuration.setPortConfigurations(
                getPortConfigurations(configuration.getConfigurationId().longValue()));
        return configuration;
    }

    @Override
    public List<AssetConfiguration> getConfigurationHistory(final long assetId) {
        return getNamedParameterJdbcTemplate().query(SELECT_HISTORY,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId)).build(),
                SharedRowMappers.ASSET_CONFIGURATION);
    }

    @Override
    public List<AssetConfiguration> getConfigurationsForCustomer(final long customerId) {
        return getNamedParameterJdbcTemplate().query(SELECT_ALL_FOR_CUSTOMER,
                ParameterRepository.of("customerId", Long.valueOf(customerId)).build(),
                SharedRowMappers.ASSET_CONFIGURATION);
    }

    @Override
    public int attachConfigurationToAsset(final long configurationId, final long assetId,
            final String userId) {
        return getNamedParameterJdbcTemplate().update(ATTACH_TO_ASSET, ParameterRepository.create()
                .with("configurationId", Long.valueOf(configurationId))
                .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                .with(CommonConstants.PARAM_USER_ID, userId)
                .build());
    }

    @Override
    public int markApplied(final long configurationId, final String userId) {
        return getNamedParameterJdbcTemplate().update(MARK_APPLIED, ParameterRepository.create()
                .with("configurationId", Long.valueOf(configurationId))
                .with(CommonConstants.PARAM_USER_ID, userId)
                .build());
    }

    @Override
    public List<String> getWanAddressesInUse() {
        return getNamedParameterJdbcTemplate().queryForList(SELECT_WAN_ADDRESSES_IN_USE,
                ParameterRepository.create().build(), String.class);
    }

    @Override
    public List<PortConfiguration> getPortConfigurations(final long configurationId) {
        return getNamedParameterJdbcTemplate().query(SELECT_PORTS,
                ParameterRepository.of("configurationId", Long.valueOf(configurationId)).build(),
                PORT_MAPPER);
    }

    @Override
    public long insertConfigurationRevision(final AssetConfiguration configuration, final String userId) {
        final Long configId = getNamedParameterJdbcTemplate().queryForObject(NEXT_CONFIG_ID,
                ParameterRepository.create().build(), Long.class);
        final Integer revision = getNamedParameterJdbcTemplate().queryForObject(NEXT_REVISION,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, configuration.getAssetId())
                        .build(), Integer.class);
        getNamedParameterJdbcTemplate().update(INSERT_CONFIG,
                ParameterRepository.create()
                        .with("configurationId", configId)
                        .with(CommonConstants.PARAM_ASSET_ID, configuration.getAssetId())
                        .with("configTypeCode", code(configuration.getAssetConfigurationType()))
                        .with(CommonConstants.PARAM_STATUS_CODE,
                                code(configuration.getAssetConfigurationStatusType()))
                        .with("networkConfigCode", code(configuration.getNetworkConfigurationType()))
                        .with("lanIpAddress", configuration.getLanIpAddress())
                        .with("lanSubnetMask", configuration.getLanSubnetMask())
                        .with("wanIpAddress", configuration.getWanIpAddress())
                        .with("wanSubnetMask", configuration.getWanSubnetMask())
                        .with("defaultGateway", configuration.getDefaultGateway())
                        .with("lanGateway", configuration.getLanGateway())
                        .with("primaryDns", configuration.getPrimaryDnsAddress())
                        .with("secondaryDns", configuration.getSecondaryDnsAddress())
                        .with("circuitId", configuration.getCircuitId())
                        .with("bandwidthKbps", configuration.getBandwidthKbps())
                        .withDate("effectiveDate", configuration.getEffectiveDate())
                        .with("revision", revision)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .build());
        configuration.setConfigurationId(configId);
        configuration.setRevision(revision == null ? 1 : revision.intValue());
        return configId.longValue();
    }

    @Override
    public AssetConfiguration getConfigurationForOrder(final long orderId) {
        final List<AssetConfiguration> rows = getNamedParameterJdbcTemplate().query(SELECT_FOR_ORDER,
                ParameterRepository.of(CommonConstants.PARAM_ORDER_ID, Long.valueOf(orderId)).build(),
                SharedRowMappers.ASSET_CONFIGURATION);
        if (rows.isEmpty()) {
            return null;
        }
        final AssetConfiguration configuration = rows.get(0);
        configuration.setPortConfigurations(
                getPortConfigurations(configuration.getConfigurationId().longValue()));
        return configuration;
    }

    @Override
    public long insertOrderConfiguration(final AssetConfiguration configuration, final long orderId,
            final String userId) {
        final Long configId = getNamedParameterJdbcTemplate().queryForObject(NEXT_CONFIG_ID,
                ParameterRepository.create().build(), Long.class);
        getNamedParameterJdbcTemplate().update(INSERT_ORDER_CONFIG,
                ParameterRepository.create()
                        .with("configurationId", configId)
                        .with(CommonConstants.PARAM_ORDER_ID, Long.valueOf(orderId))
                        .with("configTypeCode", code(configuration.getAssetConfigurationType()))
                        .with(CommonConstants.PARAM_STATUS_CODE,
                                code(configuration.getAssetConfigurationStatusType()))
                        .with("networkConfigCode", code(configuration.getNetworkConfigurationType()))
                        .with("lanIpAddress", configuration.getLanIpAddress())
                        .with("lanSubnetMask", configuration.getLanSubnetMask())
                        .with("wanIpAddress", configuration.getWanIpAddress())
                        .with("wanSubnetMask", configuration.getWanSubnetMask())
                        .with("defaultGateway", configuration.getDefaultGateway())
                        .with("lanGateway", configuration.getLanGateway())
                        .with("primaryDns", configuration.getPrimaryDnsAddress())
                        .with("secondaryDns", configuration.getSecondaryDnsAddress())
                        .with("circuitId", configuration.getCircuitId())
                        .with("bandwidthKbps", configuration.getBandwidthKbps())
                        .withDate("effectiveDate", configuration.getEffectiveDate())
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .build());
        configuration.setConfigurationId(configId);
        configuration.setRevision(1);
        return configId.longValue();
    }

    @Override
    public int markSuperseded(final long configurationId, final String userId) {
        return getNamedParameterJdbcTemplate().update(MARK_SUPERSEDED,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with("configurationId", Long.valueOf(configurationId))
                        .build());
    }

    private static String code(final org.example.am.shared.domain.LoadableType type) {
        return type == null ? null : type.getCode();
    }

    private static class PortMapper implements RowMapper<PortConfiguration> {

        @Override
        public PortConfiguration mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            final PortConfiguration port = new PortConfiguration();
            port.setPortId(ConversionUtils.getLong(rs, "PORT_ID"));
            port.setPortName(ConversionUtils.getString(rs, "PORT_NAME"));
            port.setPortConfigurationType(
                    PortConfigurationType.lookup(ConversionUtils.getString(rs, "PORT_CONFIG_CD")));
            port.setMacAddress(ConversionUtils.getString(rs, "MAC_ADDRESS"));
            port.setActive(ConversionUtils.getBoolean(rs, "ACTIVE_FL"));
            port.setVlanId(ConversionUtils.getInteger(rs, "VLAN_ID"));
            port.setIpAddress(ConversionUtils.getString(rs, "IP_ADDRESS"));
            port.setSubnetMask(ConversionUtils.getString(rs, "SUBNET_MASK"));
            return port;
        }
    }
}
