package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Progress of a scheduled decommission.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class DecommissionStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, DecommissionStatusType> VALUES = new LinkedHashMap<String, DecommissionStatusType>();

    public static final DecommissionStatusType REQUESTED = register("REQUESTED", "Requested", Long.valueOf(1L));
    public static final DecommissionStatusType SCHEDULED = register("SCHEDULED", "Scheduled", Long.valueOf(2L));
    public static final DecommissionStatusType IN_PROGRESS = register("INPROG", "In Progress", Long.valueOf(3L));
    public static final DecommissionStatusType COMPLETED = register("COMPLETED", "Completed", Long.valueOf(4L));
    public static final DecommissionStatusType CANCELLED = register("CANCELLED", "Cancelled", Long.valueOf(5L));

    private DecommissionStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static DecommissionStatusType register(final String code, final String description, final Long databaseId) {
        final DecommissionStatusType type = new DecommissionStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static DecommissionStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static DecommissionStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final DecommissionStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<DecommissionStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
