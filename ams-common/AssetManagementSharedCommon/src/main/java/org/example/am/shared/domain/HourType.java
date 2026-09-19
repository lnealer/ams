package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hour-of-day slots used by maintenance windows and timeslot pickers.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class HourType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, HourType> VALUES = new LinkedHashMap<String, HourType>();

    public static final HourType HOUR_00 = register("0000", "12:00 AM", Long.valueOf(1L));
    public static final HourType HOUR_01 = register("0100", "1:00 AM", Long.valueOf(2L));
    public static final HourType HOUR_02 = register("0200", "2:00 AM", Long.valueOf(3L));
    public static final HourType HOUR_03 = register("0300", "3:00 AM", Long.valueOf(4L));
    public static final HourType HOUR_04 = register("0400", "4:00 AM", Long.valueOf(5L));
    public static final HourType HOUR_05 = register("0500", "5:00 AM", Long.valueOf(6L));
    public static final HourType HOUR_06 = register("0600", "6:00 AM", Long.valueOf(7L));
    public static final HourType HOUR_07 = register("0700", "7:00 AM", Long.valueOf(8L));
    public static final HourType HOUR_08 = register("0800", "8:00 AM", Long.valueOf(9L));
    public static final HourType HOUR_09 = register("0900", "9:00 AM", Long.valueOf(10L));
    public static final HourType HOUR_10 = register("1000", "10:00 AM", Long.valueOf(11L));
    public static final HourType HOUR_11 = register("1100", "11:00 AM", Long.valueOf(12L));
    public static final HourType HOUR_12 = register("1200", "12:00 PM", Long.valueOf(13L));
    public static final HourType HOUR_13 = register("1300", "1:00 PM", Long.valueOf(14L));
    public static final HourType HOUR_14 = register("1400", "2:00 PM", Long.valueOf(15L));
    public static final HourType HOUR_15 = register("1500", "3:00 PM", Long.valueOf(16L));
    public static final HourType HOUR_16 = register("1600", "4:00 PM", Long.valueOf(17L));
    public static final HourType HOUR_17 = register("1700", "5:00 PM", Long.valueOf(18L));
    public static final HourType HOUR_18 = register("1800", "6:00 PM", Long.valueOf(19L));
    public static final HourType HOUR_19 = register("1900", "7:00 PM", Long.valueOf(20L));
    public static final HourType HOUR_20 = register("2000", "8:00 PM", Long.valueOf(21L));
    public static final HourType HOUR_21 = register("2100", "9:00 PM", Long.valueOf(22L));
    public static final HourType HOUR_22 = register("2200", "10:00 PM", Long.valueOf(23L));
    public static final HourType HOUR_23 = register("2300", "11:00 PM", Long.valueOf(24L));

    private HourType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static HourType register(final String code, final String description, final Long databaseId) {
        final HourType type = new HourType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static HourType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static HourType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final HourType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<HourType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
