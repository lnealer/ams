package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * State of a stored asset configuration revision.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class AssetConfigurationStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, AssetConfigurationStatusType> VALUES = new LinkedHashMap<String, AssetConfigurationStatusType>();

    public static final AssetConfigurationStatusType DRAFT = register("DRAFT", "Draft", Long.valueOf(1L));
    public static final AssetConfigurationStatusType PENDING = register("PENDING", "Pending Application", Long.valueOf(2L));
    public static final AssetConfigurationStatusType APPLIED = register("APPLIED", "Applied", Long.valueOf(3L));
    public static final AssetConfigurationStatusType MISMATCH = register("MISMATCH", "Mismatch With Device", Long.valueOf(4L));
    public static final AssetConfigurationStatusType FAILED = register("FAILED", "Failed", Long.valueOf(5L));
    public static final AssetConfigurationStatusType SUPERSEDED = register("SUPERSEDE", "Superseded", Long.valueOf(6L));

    private AssetConfigurationStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static AssetConfigurationStatusType register(final String code, final String description, final Long databaseId) {
        final AssetConfigurationStatusType type = new AssetConfigurationStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static AssetConfigurationStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static AssetConfigurationStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final AssetConfigurationStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<AssetConfigurationStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
