package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Network hardware families that AMS can order, install and decommission for a customer site.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class AssetType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, AssetType> VALUES = new LinkedHashMap<String, AssetType>();

    public static final AssetType ROUTER = register("ROUTER", "Managed Router", Long.valueOf(1L));
    public static final AssetType ROUTER_WIFI = register("ROUTERWIFI", "Managed Wi-Fi Router", Long.valueOf(2L));
    public static final AssetType ROUTER_LTE = register("ROUTERLTE", "Managed Router with LTE Failover", Long.valueOf(3L));
    public static final AssetType LEGACY_ROUTER = register("LEGACYROUTER", "Legacy Managed Router", Long.valueOf(4L));
    public static final AssetType SWITCH = register("SWITCH", "Managed Switch", Long.valueOf(5L));
    public static final AssetType SWITCH_POE = register("SWITCHPOE", "Managed PoE Switch", Long.valueOf(6L));
    public static final AssetType FIREWALL = register("FIREWALL", "Managed Firewall", Long.valueOf(7L));
    public static final AssetType ACCESS_POINT = register("ACCESSPOINT", "Wireless Access Point", Long.valueOf(8L));

    private AssetType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static AssetType register(final String code, final String description, final Long databaseId) {
        final AssetType type = new AssetType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static AssetType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static AssetType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final AssetType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<AssetType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
