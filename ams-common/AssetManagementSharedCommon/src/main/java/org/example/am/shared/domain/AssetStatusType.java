package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lifecycle state of an asset row in AMS_ASSETS.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class AssetStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, AssetStatusType> VALUES = new LinkedHashMap<String, AssetStatusType>();

    public static final AssetStatusType ORDERED = register("ORDERED", "Ordered", Long.valueOf(1L));
    public static final AssetStatusType SHIPPED = register("SHIPPED", "Shipped", Long.valueOf(2L));
    public static final AssetStatusType INSTALLED = register("INSTALLED", "Installed", Long.valueOf(3L));
    public static final AssetStatusType ACTIVE = register("ACTIVE", "Active", Long.valueOf(4L));
    public static final AssetStatusType PENDING_DECOMMISSION = register("PENDDECOM", "Pending Decommission", Long.valueOf(5L));
    public static final AssetStatusType DECOMMISSIONED = register("DECOM", "Decommissioned", Long.valueOf(6L));
    public static final AssetStatusType RETURNED = register("RETURNED", "Returned", Long.valueOf(7L));
    public static final AssetStatusType CANCELLED = register("CANCELLED", "Cancelled", Long.valueOf(8L));
    public static final AssetStatusType IN_REPAIR = register("INREPAIR", "In Repair", Long.valueOf(9L));

    private AssetStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static AssetStatusType register(final String code, final String description, final Long databaseId) {
        final AssetStatusType type = new AssetStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static AssetStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static AssetStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final AssetStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<AssetStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
