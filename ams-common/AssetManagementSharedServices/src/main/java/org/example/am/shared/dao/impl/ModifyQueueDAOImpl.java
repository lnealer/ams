package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.ModifyQueueDAO;
import org.example.am.shared.domain.QueueStatusType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * The asynchronous work queue that carries configuration changes out to the devices.
 */
@Repository("modifyQueueSharedDAO")
public class ModifyQueueDAOImpl extends BaseDAO implements ModifyQueueDAO {

    private static final String NEXT_QUEUE_ID = "SELECT AMS_MODIFY_QUEUE_SQ.NEXTVAL FROM DUAL ";

    private static final String INSERT_QUEUE_ENTRY =
            "INSERT INTO AMS_MODIFY_QUEUE "
          + "       ( QUEUE_ID, QUEUE_NAME, ENTITY_REFERENCE, PAYLOAD, QUEUE_STATUS_CD, ATTEMPT_COUNT,"
          + "         CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :queueId, :queueName, :entityReference, :payload, 'NEW', 0,"
          + "         SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String SELECT_NEXT =
            "SELECT * FROM ( SELECT Q.QUEUE_ID FROM AMS_MODIFY_QUEUE Q "
          + "                 WHERE Q.QUEUE_NAME = :queueName "
          + "                   AND Q.QUEUE_STATUS_CD IN ('NEW', 'RETRY') "
          + "                 ORDER BY Q.CREATED_DT ) WHERE ROWNUM <= :maxRows ";

    private static final String UPDATE_QUEUE_STATUS =
            "UPDATE AMS_MODIFY_QUEUE SET QUEUE_STATUS_CD = :statusCode,"
          + "       FAILURE_REASON = :failureReason, ATTEMPT_COUNT = ATTEMPT_COUNT + 1,"
          + "       MODIFIED_DT = SYSTIMESTAMP WHERE QUEUE_ID = :queueId ";

    private static final RowMapper<Long> QUEUE_ID_MAPPER = new RowMapper<Long>() {

        @Override
        public Long mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getLong(rs, "QUEUE_ID");
        }
    };

    @Override
    public long enqueue(final String queueName, final String entityReference, final String payload,
            final String userId) {
        final Long queueId = getNamedParameterJdbcTemplate().queryForObject(NEXT_QUEUE_ID,
                ParameterRepository.create().build(), Long.class);
        getNamedParameterJdbcTemplate().update(INSERT_QUEUE_ENTRY,
                ParameterRepository.create()
                        .with("queueId", queueId)
                        .with("queueName", queueName)
                        .with("entityReference", entityReference)
                        .with("payload", payload)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .build());
        return queueId.longValue();
    }

    @Override
    public List<Long> claimNext(final String queueName, final int maxRows) {
        return getNamedParameterJdbcTemplate().query(SELECT_NEXT,
                ParameterRepository.create()
                        .with("queueName", queueName)
                        .with(CommonConstants.PARAM_MAX_ROWS, Integer.valueOf(maxRows))
                        .build(), QUEUE_ID_MAPPER);
    }

    @Override
    public int updateStatus(final long queueId, final QueueStatusType status, final String failureReason) {
        return getNamedParameterJdbcTemplate().update(UPDATE_QUEUE_STATUS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_STATUS_CODE, status == null ? null : status.getCode())
                        .with("failureReason", failureReason)
                        .with("queueId", Long.valueOf(queueId))
                        .build());
    }
}
