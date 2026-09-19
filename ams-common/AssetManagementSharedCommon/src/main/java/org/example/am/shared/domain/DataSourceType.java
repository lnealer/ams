package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provenance of a record surfaced in the internal UI.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class DataSourceType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, DataSourceType> VALUES = new LinkedHashMap<String, DataSourceType>();

    public static final DataSourceType AMS = register("AMS", "AMS", Long.valueOf(1L));
    public static final DataSourceType LEGACY = register("LEGACY", "Legacy System", Long.valueOf(2L));
    public static final DataSourceType EXTERNAL_FEED = register("EXTFEED", "External Feed", Long.valueOf(3L));
    public static final DataSourceType MANUAL_ENTRY = register("MANUAL", "Manual Entry", Long.valueOf(4L));

    private DataSourceType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static DataSourceType register(final String code, final String description, final Long databaseId) {
        final DataSourceType type = new DataSourceType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static DataSourceType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static DataSourceType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final DataSourceType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<DataSourceType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
