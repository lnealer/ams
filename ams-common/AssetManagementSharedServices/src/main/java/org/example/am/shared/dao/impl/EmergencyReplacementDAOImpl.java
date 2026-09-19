package org.example.am.shared.dao.impl;

import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.EmergencyReplacementDAO;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.springframework.stereotype.Repository;

/**
 * Controls which assets may be replaced on the expedited path. The switch is per asset and is set
 * by operations, because an emergency replacement bypasses the normal lead time.
 */
@Repository("emergencyReplacementSharedDAO")
public class EmergencyReplacementDAOImpl extends BaseDAO implements EmergencyReplacementDAO {

    private static final String SELECT_ENABLED =
            "SELECT COUNT(*) FROM AMS_ASSETS A "
          + " WHERE A.ASSET_ID = :assetId AND A.EMERGENCY_REPL_FL = 'Y' ";

    private static final String UPDATE_ENABLED =
            "UPDATE AMS_ASSETS SET EMERGENCY_REPL_FL = :enabledFlag,"
          + "       MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId WHERE ASSET_ID = :assetId ";

    @Override
    public boolean isEnabled(final long assetId) {
        final Integer count = getNamedParameterJdbcTemplate().queryForObject(SELECT_ENABLED,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId)).build(),
                Integer.class);
        return count != null && count.intValue() > 0;
    }

    @Override
    public int setEnabled(final long assetId, final boolean enabled, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_ENABLED,
                ParameterRepository.create()
                        .withFlag("enabledFlag", enabled)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId))
                        .build());
    }
}
