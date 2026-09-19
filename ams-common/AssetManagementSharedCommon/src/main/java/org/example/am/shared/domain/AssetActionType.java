package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Actions the internal UI can offer against an asset.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class AssetActionType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, AssetActionType> VALUES = new LinkedHashMap<String, AssetActionType>();

    public static final AssetActionType ORDER = register("ORDER", "Order", Long.valueOf(1L));
    public static final AssetActionType MOVE = register("MOVE", "Move", Long.valueOf(2L));
    public static final AssetActionType DECOMMISSION = register("DECOM", "Decommission", Long.valueOf(3L));
    public static final AssetActionType MODIFY_CONFIG = register("MODCFG", "Modify Configuration", Long.valueOf(4L));
    public static final AssetActionType REPLACE = register("REPLACE", "Replace", Long.valueOf(5L));
    public static final AssetActionType RMA = register("RMA", "Create RMA", Long.valueOf(6L));
    public static final AssetActionType COMPARE_CONFIG = register("CMPCFG", "Compare Configuration", Long.valueOf(7L));

    private AssetActionType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static AssetActionType register(final String code, final String description, final Long databaseId) {
        final AssetActionType type = new AssetActionType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static AssetActionType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static AssetActionType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final AssetActionType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<AssetActionType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
