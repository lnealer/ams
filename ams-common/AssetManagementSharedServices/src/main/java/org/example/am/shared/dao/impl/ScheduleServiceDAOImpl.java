package org.example.am.shared.dao.impl;

import java.util.Date;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.ScheduleServiceDAO;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.springframework.stereotype.Repository;

/**
 * Reads and writes the scheduled dates held against orders, change requests and decommissions.
 */
@Repository("scheduleServiceSharedDAO")
public class ScheduleServiceDAOImpl extends BaseDAO implements ScheduleServiceDAO {

    private static final String UPDATE_INSTALL_DATE =
            "UPDATE AMS_INSTALLATIONS SET SCHEDULED_DT = :scheduledDate,"
          + "       INSTALL_STATUS_CD = 'SCHEDULED', MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE ORDER_ID = :orderId ";

    private static final String UPDATE_NCR_DATE =
            "UPDATE AMS_NETWORK_CHANGE_REQUESTS SET SCHEDULED_DT = :scheduledDate,"
          + "       NCR_STATUS_CD = 'SCHEDULED', MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE NCR_ID = :networkChangeRequestId ";

    private static final String UPDATE_DECOM_DATE =
            "UPDATE AMS_DECOMMISSIONS SET SCHEDULED_DT = :scheduledDate,"
          + "       DECOM_STATUS_CD = 'SCHEDULED', MODIFIED_DT = SYSTIMESTAMP, MODIFIED_BY = :userId "
          + " WHERE DECOMMISSION_ID = :decommissionId ";

    @Override
    public int updateInstallationDate(final long orderId, final Date scheduledDate, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_INSTALL_DATE,
                ParameterRepository.create()
                        .withDate("scheduledDate", scheduledDate)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_ORDER_ID, Long.valueOf(orderId))
                        .build());
    }

    @Override
    public int updateNetworkChangeDate(final long networkChangeRequestId, final Date scheduledDate,
            final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_NCR_DATE,
                ParameterRepository.create()
                        .withDate("scheduledDate", scheduledDate)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with(CommonConstants.PARAM_NCR_ID, Long.valueOf(networkChangeRequestId))
                        .build());
    }

    @Override
    public int updateDecommissionDate(final long decommissionId, final Date scheduledDate,
            final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_DECOM_DATE,
                ParameterRepository.create()
                        .withDate("scheduledDate", scheduledDate)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with("decommissionId", Long.valueOf(decommissionId))
                        .build());
    }
}
