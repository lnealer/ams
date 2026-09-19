package org.example.am.shared.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.EmailDetailDAO;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.EmailQueueStatusType;
import org.example.am.shared.domain.EmailTemplateType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.example.am.shared.utils.ConversionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Reads and writes the outbound email queue. Mail is never sent inline: a row is queued inside the
 * business transaction and a separate poller delivers it, so a mail server outage cannot roll back an
 * order.
 */
@Repository("emailDetailSharedDAO")
public class EmailDetailDAOImpl extends BaseDAO implements EmailDetailDAO {

    private static final String NEXT_EMAIL_ID = "SELECT AMS_EMAIL_QUEUE_SQ.NEXTVAL FROM DUAL ";

    private static final String INSERT_EMAIL =
            "INSERT INTO AMS_EMAIL_QUEUE "
          + "       ( EMAIL_ID, TEMPLATE_CD, ENTITY_TYPE_CD, ENTITY_ID, TO_ADDRESS,"
          + "         EMAIL_STATUS_CD, CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY ) "
          + "VALUES ( :emailId, :templateCode, :entityTypeCode, :entityId, :toAddress,"
          + "         'QUEUED', SYSTIMESTAMP, :userId, SYSTIMESTAMP, :userId ) ";

    private static final String SELECT_QUEUED =
            "SELECT * FROM ( SELECT E.EMAIL_ID FROM AMS_EMAIL_QUEUE E "
          + "                 WHERE E.EMAIL_STATUS_CD = 'QUEUED' ORDER BY E.CREATED_DT ) "
          + " WHERE ROWNUM <= :maxRows ";

    private static final String UPDATE_EMAIL_STATUS =
            "UPDATE AMS_EMAIL_QUEUE SET EMAIL_STATUS_CD = :statusCode,"
          + "       FAILURE_REASON = :failureReason, MODIFIED_DT = SYSTIMESTAMP "
          + " WHERE EMAIL_ID = :emailId ";

    private static final RowMapper<Long> EMAIL_ID_MAPPER = new RowMapper<Long>() {

        @Override
        public Long mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
            return ConversionUtils.getLong(rs, "EMAIL_ID");
        }
    };

    @Override
    public long queueEmail(final EmailTemplateType template, final EmailEntityType entityType,
            final long entityId, final String toAddress, final String userId) {
        final Long emailId = getNamedParameterJdbcTemplate().queryForObject(NEXT_EMAIL_ID,
                ParameterRepository.create().build(), Long.class);
        getNamedParameterJdbcTemplate().update(INSERT_EMAIL,
                ParameterRepository.create()
                        .with("emailId", emailId)
                        .with("templateCode", template == null ? null : template.getCode())
                        .with("entityTypeCode", entityType == null ? null : entityType.getCode())
                        .with("entityId", Long.valueOf(entityId))
                        .with("toAddress", toAddress)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .build());
        return emailId.longValue();
    }

    @Override
    public List<Long> getQueuedEmailIds(final int maxRows) {
        return getNamedParameterJdbcTemplate().query(SELECT_QUEUED,
                ParameterRepository.of(CommonConstants.PARAM_MAX_ROWS, Integer.valueOf(maxRows)).build(),
                EMAIL_ID_MAPPER);
    }

    @Override
    public int updateStatus(final long emailId, final EmailQueueStatusType status, final String failureReason) {
        return getNamedParameterJdbcTemplate().update(UPDATE_EMAIL_STATUS,
                ParameterRepository.create()
                        .with(CommonConstants.PARAM_STATUS_CODE, status == null ? null : status.getCode())
                        .with("failureReason", failureReason)
                        .with("emailId", Long.valueOf(emailId))
                        .build());
    }
}
