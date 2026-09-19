package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Subscription state of a customer service.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class ServiceStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, ServiceStatusType> VALUES = new LinkedHashMap<String, ServiceStatusType>();

    public static final ServiceStatusType PENDING = register("PENDING", "Pending", Long.valueOf(1L));
    public static final ServiceStatusType ACTIVE = register("ACTIVE", "Active", Long.valueOf(2L));
    public static final ServiceStatusType SUSPENDED = register("SUSPENDED", "Suspended", Long.valueOf(3L));
    public static final ServiceStatusType TERMINATED = register("TERMINATED", "Terminated", Long.valueOf(4L));

    private ServiceStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static ServiceStatusType register(final String code, final String description, final Long databaseId) {
        final ServiceStatusType type = new ServiceStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static ServiceStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static ServiceStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final ServiceStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<ServiceStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
