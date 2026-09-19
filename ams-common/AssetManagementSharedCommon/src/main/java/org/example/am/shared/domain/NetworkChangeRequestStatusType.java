package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lifecycle state of a network change request.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class NetworkChangeRequestStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, NetworkChangeRequestStatusType> VALUES = new LinkedHashMap<String, NetworkChangeRequestStatusType>();

    public static final NetworkChangeRequestStatusType DRAFT = register("DRAFT", "Draft", Long.valueOf(1L));
    public static final NetworkChangeRequestStatusType SAVED = register("SAVED", "Saved For Later", Long.valueOf(2L));
    public static final NetworkChangeRequestStatusType SUBMITTED = register("SUBMITTED", "Submitted", Long.valueOf(3L));
    public static final NetworkChangeRequestStatusType SCHEDULED = register("SCHEDULED", "Scheduled", Long.valueOf(4L));
    public static final NetworkChangeRequestStatusType IN_PROGRESS = register("INPROG", "In Progress", Long.valueOf(5L));
    public static final NetworkChangeRequestStatusType COMPLETED = register("COMPLETED", "Completed", Long.valueOf(6L));
    public static final NetworkChangeRequestStatusType CANCELLED = register("CANCELLED", "Cancelled", Long.valueOf(7L));

    private NetworkChangeRequestStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static NetworkChangeRequestStatusType register(final String code, final String description, final Long databaseId) {
        final NetworkChangeRequestStatusType type = new NetworkChangeRequestStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static NetworkChangeRequestStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static NetworkChangeRequestStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final NetworkChangeRequestStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<NetworkChangeRequestStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
