package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * An audit trail entry surfaced on the asset and order history tabs.
 */
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long eventId;
    private EventType eventType;
    private Date eventDate;
    private String userId;
    private String userDisplayName;
    private String description;
    private Long entityId;
    private EmailEntityType entityType;
    private DataSourceType dataSourceType;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(final Long eventId) {
        this.eventId = eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(final EventType eventType) {
        this.eventType = eventType;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(final Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(final String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(final Long entityId) {
        this.entityId = entityId;
    }

    public EmailEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(final EmailEntityType entityType) {
        this.entityType = entityType;
    }

    public DataSourceType getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(final DataSourceType dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

}
