package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * How an interface obtains and advertises its addressing.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class NetworkConfigurationType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, NetworkConfigurationType> VALUES = new LinkedHashMap<String, NetworkConfigurationType>();

    public static final NetworkConfigurationType STATIC = register("STATIC", "Static", Long.valueOf(1L));
    public static final NetworkConfigurationType DHCP = register("DHCP", "DHCP", Long.valueOf(2L));
    public static final NetworkConfigurationType PPPOE = register("PPPOE", "PPPoE", Long.valueOf(3L));
    public static final NetworkConfigurationType BGP = register("BGP", "BGP", Long.valueOf(4L));
    public static final NetworkConfigurationType OSPF = register("OSPF", "OSPF", Long.valueOf(5L));
    public static final NetworkConfigurationType STATIC_ROUTE = register("STATICRT", "Static Route", Long.valueOf(6L));

    private NetworkConfigurationType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static NetworkConfigurationType register(final String code, final String description, final Long databaseId) {
        final NetworkConfigurationType type = new NetworkConfigurationType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static NetworkConfigurationType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static NetworkConfigurationType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final NetworkConfigurationType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<NetworkConfigurationType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
