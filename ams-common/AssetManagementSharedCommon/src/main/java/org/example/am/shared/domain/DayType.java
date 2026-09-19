package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Days of the week used by maintenance windows and calendars.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class DayType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, DayType> VALUES = new LinkedHashMap<String, DayType>();

    public static final DayType SUNDAY = register("SUN", "Sunday", Long.valueOf(1L));
    public static final DayType MONDAY = register("MON", "Monday", Long.valueOf(2L));
    public static final DayType TUESDAY = register("TUE", "Tuesday", Long.valueOf(3L));
    public static final DayType WEDNESDAY = register("WED", "Wednesday", Long.valueOf(4L));
    public static final DayType THURSDAY = register("THU", "Thursday", Long.valueOf(5L));
    public static final DayType FRIDAY = register("FRI", "Friday", Long.valueOf(6L));
    public static final DayType SATURDAY = register("SAT", "Saturday", Long.valueOf(7L));

    private DayType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static DayType register(final String code, final String description, final Long databaseId) {
        final DayType type = new DayType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static DayType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static DayType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final DayType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<DayType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
