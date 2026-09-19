package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * State of a row in the outbound email queue.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class EmailQueueStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, EmailQueueStatusType> VALUES = new LinkedHashMap<String, EmailQueueStatusType>();

    public static final EmailQueueStatusType QUEUED = register("QUEUED", "Queued", Long.valueOf(1L));
    public static final EmailQueueStatusType SENDING = register("SENDING", "Sending", Long.valueOf(2L));
    public static final EmailQueueStatusType SENT = register("SENT", "Sent", Long.valueOf(3L));
    public static final EmailQueueStatusType FAILED = register("FAILED", "Failed", Long.valueOf(4L));
    public static final EmailQueueStatusType SUPPRESSED = register("SUPPRESS", "Suppressed", Long.valueOf(5L));

    private EmailQueueStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static EmailQueueStatusType register(final String code, final String description, final Long databaseId) {
        final EmailQueueStatusType type = new EmailQueueStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static EmailQueueStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static EmailQueueStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final EmailQueueStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<EmailQueueStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
