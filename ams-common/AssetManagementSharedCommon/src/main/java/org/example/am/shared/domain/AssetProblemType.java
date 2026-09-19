package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reasons an asset is ineligible for a requested action.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class AssetProblemType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, AssetProblemType> VALUES = new LinkedHashMap<String, AssetProblemType>();

    public static final AssetProblemType NOT_MIGRATABLE = register("NOTMIGRATE", "Asset is not migratable", Long.valueOf(1L));
    public static final AssetProblemType PENDING_ORDER = register("PENDORDER", "Asset has a pending order", Long.valueOf(2L));
    public static final AssetProblemType PENDING_NCR = register("PENDNCR", "Asset has a pending network change request", Long.valueOf(3L));
    public static final AssetProblemType DECOMMISSION_SCHEDULED = register("DECOMSCHED", "Asset has a scheduled decommission", Long.valueOf(4L));
    public static final AssetProblemType CONFIG_MISMATCH = register("CFGMISMATCH", "Asset configuration does not match the device", Long.valueOf(5L));
    public static final AssetProblemType NO_ACTIVE_SERVICE = register("NOSERVICE", "Customer has no active service for this asset", Long.valueOf(6L));

    private AssetProblemType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static AssetProblemType register(final String code, final String description, final Long databaseId) {
        final AssetProblemType type = new AssetProblemType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static AssetProblemType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static AssetProblemType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final AssetProblemType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<AssetProblemType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
