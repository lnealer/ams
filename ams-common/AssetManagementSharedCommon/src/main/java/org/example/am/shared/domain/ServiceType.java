package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Managed network services a customer may subscribe to.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class ServiceType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, ServiceType> VALUES = new LinkedHashMap<String, ServiceType>();

    public static final ServiceType MANAGED_ROUTER = register("MGDROUTER", "Managed Router Service", Long.valueOf(1L));
    public static final ServiceType MANAGED_WIFI = register("MGDWIFI", "Managed Wi-Fi Service", Long.valueOf(2L));
    public static final ServiceType SD_WAN = register("SDWAN", "SD-WAN Service", Long.valueOf(3L));
    public static final ServiceType MONITORING = register("MONITOR", "Network Monitoring Service", Long.valueOf(4L));
    public static final ServiceType SUPPORT_24X7 = register("SUPPORT247", "24x7 Support Service", Long.valueOf(5L));

    private ServiceType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static ServiceType register(final String code, final String description, final Long databaseId) {
        final ServiceType type = new ServiceType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static ServiceType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static ServiceType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final ServiceType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<ServiceType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
