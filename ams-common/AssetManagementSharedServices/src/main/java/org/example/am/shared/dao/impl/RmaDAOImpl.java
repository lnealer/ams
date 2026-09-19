package org.example.am.shared.dao.impl;

import java.util.List;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.RmaDAO;
import org.example.am.shared.domain.Rma;
import org.example.am.shared.domain.RmaStatusType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.springframework.stereotype.Repository;

/**
 * Reads and writes {@code AMS_RMAS}.
 */
@Repository("rmaSharedDAO")
public class RmaDAOImpl extends BaseDAO implements RmaDAO {

    private static final String RMA_COLUMNS =
            "  R.RMA_ID, R.RMA_NUMBER, R.ASSET_ID, R.ASSET_TAG, R.TRACKING_NUMBER,"
          + "  R.SHIPPING_CARRIER_CD, R.RMA_STATUS_CD, R.ISSUED_DT, R.DUE_DT, R.RECEIVED_DT, R.REASON ";

    private static final String SELECT_RMA =
            "SELECT " + RMA_COLUMNS + " FROM AMS_RMAS R WHERE R.RMA_ID = :rmaId ";

    private static final String SELECT_FOR_ASSET =
            "SELECT " + RMA_COLUMNS
          + "  FROM AMS_RMAS R WHERE R.ASSET_ID = :assetId ORDER BY R.ISSUED_DT DESC ";

    /** Hardware that has not come back inside the return window, for the chase-up report. */
    private static final String SELECT_OVERDUE =
            "SELECT " + RMA_COLUMNS
          + "  FROM AMS_RMAS R "
          + " WHERE R.RECEIVED_DT IS NULL "
          + "   AND R.DUE_DT < SYSTIMESTAMP "
          + "   AND R.RMA_STATUS_CD NOT IN ('CLOSED', 'RECEIVED') "
          + " ORDER BY R.DUE_DT ";

    private static final String UPDATE_STATUS =
            "UPDATE AMS_RMAS SET RMA_STATUS_CD = :statusCode, MODIFIED_DT = SYSTIMESTAMP,"
          + "       MODIFIED_BY = :userId WHERE RMA_ID = :rmaId ";

    @Override
    public Rma getRma(final long rmaId) {
        final List<Rma> rows = getNamedParameterJdbcTemplate().query(SELECT_RMA,
                ParameterRepository.of("rmaId", Long.valueOf(rmaId)).build(), SharedRowMappers.RMA);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<Rma> getRmasForAsset(final long assetId) {
        return getNamedParameterJdbcTemplate().query(SELECT_FOR_ASSET,
                ParameterRepository.of(CommonConstants.PARAM_ASSET_ID, Long.valueOf(assetId)).build(),
                SharedRowMappers.RMA);
    }

    @Override
    public List<Rma> getOverdueRmas() {
        return getNamedParameterJdbcTemplate().query(SELECT_OVERDUE,
                ParameterRepository.create().build(), SharedRowMappers.RMA);
    }

    @Override
    public int updateStatus(final long rmaId, final RmaStatusType status, final String userId) {
        return getNamedParameterJdbcTemplate().update(UPDATE_STATUS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_STATUS_CODE, status == null ? null : status.getCode())
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .with("rmaId", Long.valueOf(rmaId))
                        .build());
    }
}
