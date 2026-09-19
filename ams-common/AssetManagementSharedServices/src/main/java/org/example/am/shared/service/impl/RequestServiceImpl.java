package org.example.am.shared.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.example.am.shared.dao.RequestDAO;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.Event;
import org.example.am.shared.domain.EventType;
import org.example.am.shared.domain.comparator.EventComparator;
import org.example.am.shared.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes and reads the audit trail.
 */
@Service("requestService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class RequestServiceImpl implements RequestService {

    @Autowired
    private RequestDAO requestDAO;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long recordEvent(final EventType eventType, final EmailEntityType entityType,
            final long entityId, final String description, final String userId) {
        return requestDAO.recordEvent(eventType, entityType, entityId, description, userId);
    }

    @Override
    public List<Event> getHistory(final EmailEntityType entityType, final long entityId) {
        final List<Event> events = new ArrayList<Event>(requestDAO.getEvents(entityType, entityId));
        Collections.sort(events, new EventComparator());
        return events;
    }
}
