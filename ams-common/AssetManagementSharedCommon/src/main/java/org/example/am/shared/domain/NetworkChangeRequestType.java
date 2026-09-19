package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kind of network change requested; SITE_TYPE_CHANGE routes to the complex scheduling procedure.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class NetworkChangeRequestType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, NetworkChangeRequestType> VALUES = new LinkedHashMap<String, NetworkChangeRequestType>();

    public static final NetworkChangeRequestType SITE_TYPE_CHANGE = register("SITETYPE", "Site Type Change", Long.valueOf(1L));
    public static final NetworkChangeRequestType IP_READDRESS = register("IPREADDR", "IP Re-address", Long.valueOf(2L));
    public static final NetworkChangeRequestType BANDWIDTH_CHANGE = register("BANDWIDTH", "Bandwidth Change", Long.valueOf(3L));
    public static final NetworkChangeRequestType MOVE = register("MOVE", "Move", Long.valueOf(4L));
    public static final NetworkChangeRequestType LAN_RECONFIGURATION = register("LANRECFG", "LAN Reconfiguration", Long.valueOf(5L));
    public static final NetworkChangeRequestType CIRCUIT_REPLACEMENT = register("CIRCUITREPL", "Circuit Replacement", Long.valueOf(6L));

    private NetworkChangeRequestType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static NetworkChangeRequestType register(final String code, final String description, final Long databaseId) {
        final NetworkChangeRequestType type = new NetworkChangeRequestType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static NetworkChangeRequestType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static NetworkChangeRequestType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final NetworkChangeRequestType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<NetworkChangeRequestType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
