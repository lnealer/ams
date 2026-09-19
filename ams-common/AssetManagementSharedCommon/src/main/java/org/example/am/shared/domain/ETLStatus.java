package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Outcome of a nightly extract/transform/load run.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class ETLStatus extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, ETLStatus> VALUES = new LinkedHashMap<String, ETLStatus>();

    public static final ETLStatus NOT_STARTED = register("NOTSTART", "Not Started", Long.valueOf(1L));
    public static final ETLStatus RUNNING = register("RUNNING", "Running", Long.valueOf(2L));
    public static final ETLStatus SUCCESS = register("SUCCESS", "Success", Long.valueOf(3L));
    public static final ETLStatus FAILED = register("FAILED", "Failed", Long.valueOf(4L));
    public static final ETLStatus PARTIAL = register("PARTIAL", "Partial Success", Long.valueOf(5L));

    private ETLStatus(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static ETLStatus register(final String code, final String description, final Long databaseId) {
        final ETLStatus type = new ETLStatus(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static ETLStatus lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static ETLStatus lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final ETLStatus type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<ETLStatus> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
