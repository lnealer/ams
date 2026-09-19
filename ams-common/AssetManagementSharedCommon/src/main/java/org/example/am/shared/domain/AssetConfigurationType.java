package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shape of the network configuration held against an asset.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class AssetConfigurationType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, AssetConfigurationType> VALUES = new LinkedHashMap<String, AssetConfigurationType>();

    public static final AssetConfigurationType LAN_TYPE_A = register("LANA", "LAN Type A", Long.valueOf(1L));
    public static final AssetConfigurationType LAN_TYPE_B = register("LANB", "LAN Type B", Long.valueOf(2L));
    public static final AssetConfigurationType LAN_TYPE_C = register("LANC", "LAN Type C", Long.valueOf(3L));
    public static final AssetConfigurationType WAN = register("WAN", "WAN", Long.valueOf(4L));
    public static final AssetConfigurationType DUAL_WAN = register("DUALWAN", "Dual WAN", Long.valueOf(5L));
    public static final AssetConfigurationType HA_PAIR = register("HAPAIR", "High Availability Pair", Long.valueOf(6L));

    private AssetConfigurationType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static AssetConfigurationType register(final String code, final String description, final Long databaseId) {
        final AssetConfigurationType type = new AssetConfigurationType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static AssetConfigurationType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static AssetConfigurationType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final AssetConfigurationType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<AssetConfigurationType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
