package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Speed/duplex setting applied to a physical port.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class PortConfigurationType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, PortConfigurationType> VALUES = new LinkedHashMap<String, PortConfigurationType>();

    public static final PortConfigurationType AUTO = register("AUTO", "Auto Negotiate", Long.valueOf(1L));
    public static final PortConfigurationType HALF_10 = register("10HALF", "10 Mbps Half Duplex", Long.valueOf(2L));
    public static final PortConfigurationType FULL_10 = register("10FULL", "10 Mbps Full Duplex", Long.valueOf(3L));
    public static final PortConfigurationType HALF_100 = register("100HALF", "100 Mbps Half Duplex", Long.valueOf(4L));
    public static final PortConfigurationType FULL_100 = register("100FULL", "100 Mbps Full Duplex", Long.valueOf(5L));
    public static final PortConfigurationType FULL_1000 = register("1000FULL", "1 Gbps Full Duplex", Long.valueOf(6L));
    public static final PortConfigurationType DISABLED = register("DISABLED", "Disabled", Long.valueOf(7L));

    private PortConfigurationType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static PortConfigurationType register(final String code, final String description, final Long databaseId) {
        final PortConfigurationType type = new PortConfigurationType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static PortConfigurationType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static PortConfigurationType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final PortConfigurationType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<PortConfigurationType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
