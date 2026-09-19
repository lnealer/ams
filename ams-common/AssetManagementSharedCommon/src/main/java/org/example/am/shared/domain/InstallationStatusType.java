package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Progress of a scheduled installation visit.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class InstallationStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, InstallationStatusType> VALUES = new LinkedHashMap<String, InstallationStatusType>();

    public static final InstallationStatusType NOT_SCHEDULED = register("NOTSCHED", "Not Scheduled", Long.valueOf(1L));
    public static final InstallationStatusType SCHEDULED = register("SCHEDULED", "Scheduled", Long.valueOf(2L));
    public static final InstallationStatusType RESCHEDULED = register("RESCHED", "Rescheduled", Long.valueOf(3L));
    public static final InstallationStatusType IN_PROGRESS = register("INPROG", "In Progress", Long.valueOf(4L));
    public static final InstallationStatusType COMPLETED = register("COMPLETED", "Completed", Long.valueOf(5L));
    public static final InstallationStatusType FAILED = register("FAILED", "Failed", Long.valueOf(6L));
    public static final InstallationStatusType CANCELLED = register("CANCELLED", "Cancelled", Long.valueOf(7L));

    private InstallationStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static InstallationStatusType register(final String code, final String description, final Long databaseId) {
        final InstallationStatusType type = new InstallationStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static InstallationStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static InstallationStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final InstallationStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<InstallationStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
