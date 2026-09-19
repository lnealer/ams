package org.example.am.shared.dao.impl;

import java.util.List;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.ModifyConfigurationDAO;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.springframework.stereotype.Repository;

/**
 * Supports the modify-configuration flow: what the device currently reports, versus the revision
 * AMS believes is applied.
 */
@Repository("modifyConfigurationSharedDAO")
public class ModifyConfigurationDAOImpl extends BaseDAO implements ModifyConfigurationDAO {

    /**
     * The device-reported configuration is loaded by the nightly poll into the same table shape as
     * the stored revision, distinguished by a data source column, so the compare screen can diff two
     * objects of the same type.
     */
    private static final String SELECT_DEVICE_REPORTED =
            "SELECT * FROM ( SELECT G.CONFIG_ID, G.ASSET_ID, G.CONFIG_TYPE_CD, G.CONFIG_STATUS_CD,"
          + "                       G.NETWORK_CONFIG_CD, G.LAN_IP_ADDRESS, G.LAN_SUBNET_MASK,"
          + "                       G.WAN_IP_ADDRESS, G.WAN_SUBNET_MASK, G.DEFAULT_GATEWAY,"
          + "                       G.PRIMARY_DNS, G.SECONDARY_DNS, G.CIRCUIT_ID, G.BANDWIDTH_KBPS,"
          + "                       G.EFFECTIVE_DT, G.REVISION_NUM "
          + "                  FROM AMS_ASSET_CONFIGS G "
          + "                 WHERE G.ASSET_ID = :assetId "
          + "                   AND G.DATA_SOURCE_CD = 'EXTFEED' "
          + "                 ORDER BY G.EFFECTIVE_DT DESC ) WHERE ROWNUM <= 1 ";

    private static final String MARK_MISMATCH =
            "UPDATE AMS_ASSET_CONFIGS SET CONFIG_STATUS_CD = 'MISMATCH',"
          + "       MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId WHERE CONFIG_ID = :configurationId ";

    private static final String RESOLVE_MISMATCH =
            "UPDATE AMS_ASSET_CONFIGS SET CONFIG_STATUS_CD = 'APPLIED',"
          + "       MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE CONFIG_ID = :configurationId AND CONFIG_STATUS_CD = 'MISMATCH' ";

    @Override
    public AssetConfiguration getDeviceReportedConfiguration(final long assetId) {
        final List<AssetConfiguration> rows = getNamedParameterJdbcTemplate().query(
                SELECT_DEVICE_REPORTED,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId)).build(),
                SharedRowMappers.ASSET_CONFIGURATION);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public int markMismatch(final long configurationId, final String userId) {
        return getNamedParameterJdbcTemplate().update(MARK_MISMATCH,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with("configurationId", Long.valueOf(configurationId))
                        .build());
    }

    @Override
    public int resolveMismatch(final long configurationId, final String userId) {
        return getNamedParameterJdbcTemplate().update(RESOLVE_MISMATCH,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with("configurationId", Long.valueOf(configurationId))
                        .build());
    }
}
