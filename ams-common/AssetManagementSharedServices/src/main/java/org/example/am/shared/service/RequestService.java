package org.example.am.shared.service;

import java.util.List;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.Event;
import org.example.am.shared.domain.EventType;

/**
 * Writes and reads the audit trail.
 */
public interface RequestService {

    long recordEvent(EventType eventType, EmailEntityType entityType, long entityId,
                String description, String userId);

    /** @return the entity's events, newest first */
    List<Event> getHistory(EmailEntityType entityType, long entityId);
}
