package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Countries AMS ships hardware to.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class CountryType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, CountryType> VALUES = new LinkedHashMap<String, CountryType>();

    public static final CountryType US = register("US", "United States", Long.valueOf(1L));
    public static final CountryType CA = register("CA", "Canada", Long.valueOf(2L));
    public static final CountryType MX = register("MX", "Mexico", Long.valueOf(3L));
    public static final CountryType GB = register("GB", "United Kingdom", Long.valueOf(4L));
    public static final CountryType DE = register("DE", "Germany", Long.valueOf(5L));
    public static final CountryType JP = register("JP", "Japan", Long.valueOf(6L));
    public static final CountryType AU = register("AU", "Australia", Long.valueOf(7L));
    public static final CountryType PR = register("PR", "Puerto Rico", Long.valueOf(8L));
    public static final CountryType VI = register("VI", "U.S. Virgin Islands", Long.valueOf(9L));
    public static final CountryType GU = register("GU", "Guam", Long.valueOf(10L));

    private CountryType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static CountryType register(final String code, final String description, final Long databaseId) {
        final CountryType type = new CountryType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static CountryType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static CountryType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final CountryType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<CountryType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
