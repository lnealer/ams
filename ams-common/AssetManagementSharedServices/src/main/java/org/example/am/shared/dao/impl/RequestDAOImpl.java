package org.example.am.shared.dao.impl;

import java.util.List;
import org.example.am.shared.dao.BaseDAO;
import org.example.am.shared.dao.RequestDAO;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.Event;
import org.example.am.shared.domain.EventType;
import org.example.am.shared.helper.ParameterRepository;
import org.example.am.shared.utils.CommonConstants;
import org.springframework.stereotype.Repository;

/**
 * Writes the audit events that the asset, order and change request history tabs render.
 */
@Repository("requestSharedDAO")
public class RequestDAOImpl extends BaseDAO implements RequestDAO {

    private static final String NEXT_EVENT_ID =
            "SELECT " + CommonConstants.SEQ_EVENTS + ".NEXTVAL FROM DUAL ";

    private static final String INSERT_EVENT =
            "INSERT INTO AMS_EVENTS "
          + "       ( EVENT_ID, EVENT_TYPE_CD, ENTITY_TYPE_CD, ENTITY_ID, DESCRIPTION,"
          + "         USER_ID, USER_DISPLAY_NAME, DATA_SOURCE_CD, EVENT_DT ) "
          + "VALUES ( :eventId, :eventTypeCode, :entityTypeCode, :entityId, :description,"
          + "         :userId, :userId, 'AMS', SYSTIMESTAMP ) ";

    private static final String SELECT_EVENTS =
            "SELECT E.EVENT_ID, E.EVENT_TYPE_CD, E.EVENT_DT, E.USER_ID, E.USER_DISPLAY_NAME,"
          + "       E.DESCRIPTION, E.ENTITY_ID, E.ENTITY_TYPE_CD, E.DATA_SOURCE_CD "
          + "  FROM AMS_EVENTS E "
          + " WHERE E.ENTITY_TYPE_CD = :entityTypeCode AND E.ENTITY_ID = :entityId "
          + " ORDER BY E.EVENT_DT DESC, E.EVENT_ID DESC ";

    @Override
    public long recordEvent(final EventType eventType, final EmailEntityType entityType,
            final long entityId, final String description, final String userId) {
        final Long eventId = getNamedParameterJdbcTemplate().queryForObject(NEXT_EVENT_ID,
                ParameterRepository.create().build(), Long.class);
        getNamedParameterJdbcTemplate().update(INSERT_EVENT,
                ParameterRepository.create()
                        .with("eventId", eventId)
                        .with("eventTypeCode", eventType == null ? null : eventType.getCode())
                        .with("entityTypeCode", entityType == null ? null : entityType.getCode())
                        .with("entityId", Long.valueOf(entityId))
                        .with("description", description)
                        .with(CommonConstants.PARAM_USER_ID, userId)
                        .build());
        return eventId.longValue();
    }

    @Override
    public List<Event> getEvents(final EmailEntityType entityType, final long entityId) {
        return getNamedParameterJdbcTemplate().query(SELECT_EVENTS,
                ParameterRepository.create()
                        .with("entityTypeCode", entityType == null ? null : entityType.getCode())
                        .with("entityId", Long.valueOf(entityId))
                        .build(), SharedRowMappers.EVENT);
    }
}
