package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Severity styling applied to a grid row or banner.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class DisplayStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, DisplayStatusType> VALUES = new LinkedHashMap<String, DisplayStatusType>();

    public static final DisplayStatusType NORMAL = register("NORMAL", "Normal", Long.valueOf(1L));
    public static final DisplayStatusType INFORMATIONAL = register("INFO", "Informational", Long.valueOf(2L));
    public static final DisplayStatusType WARNING = register("WARNING", "Warning", Long.valueOf(3L));
    public static final DisplayStatusType ATTENTION = register("ATTENTION", "Needs Attention", Long.valueOf(4L));
    public static final DisplayStatusType ERROR = register("ERROR", "Error", Long.valueOf(5L));

    private DisplayStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static DisplayStatusType register(final String code, final String description, final Long databaseId) {
        final DisplayStatusType type = new DisplayStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static DisplayStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static DisplayStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final DisplayStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<DisplayStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
