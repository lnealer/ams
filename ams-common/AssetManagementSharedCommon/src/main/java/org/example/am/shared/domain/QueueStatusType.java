package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic asynchronous work queue state.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class QueueStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, QueueStatusType> VALUES = new LinkedHashMap<String, QueueStatusType>();

    public static final QueueStatusType NEW = register("NEW", "New", Long.valueOf(1L));
    public static final QueueStatusType IN_PROGRESS = register("INPROG", "In Progress", Long.valueOf(2L));
    public static final QueueStatusType COMPLETED = register("COMPLETED", "Completed", Long.valueOf(3L));
    public static final QueueStatusType FAILED = register("FAILED", "Failed", Long.valueOf(4L));
    public static final QueueStatusType RETRY = register("RETRY", "Awaiting Retry", Long.valueOf(5L));

    private QueueStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static QueueStatusType register(final String code, final String description, final Long databaseId) {
        final QueueStatusType type = new QueueStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static QueueStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static QueueStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final QueueStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<QueueStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
