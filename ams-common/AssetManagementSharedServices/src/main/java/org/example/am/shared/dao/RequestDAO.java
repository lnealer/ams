package org.example.am.shared.dao;

import java.util.List;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.Event;
import org.example.am.shared.domain.EventType;

/**
 * Writes the audit events that the asset, order and change request history tabs render.
 */
public interface RequestDAO {

    long recordEvent(EventType eventType, EmailEntityType entityType, long entityId,
                String description, String userId);

    List<Event> getEvents(EmailEntityType entityType, long entityId);
}
