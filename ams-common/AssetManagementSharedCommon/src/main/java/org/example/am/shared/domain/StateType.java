package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * US states and territories an asset may be installed in.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class StateType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, StateType> VALUES = new LinkedHashMap<String, StateType>();

    public static final StateType ALABAMA = register("AL", "Alabama", Long.valueOf(1L));
    public static final StateType ALASKA = register("AK", "Alaska", Long.valueOf(2L));
    public static final StateType ARIZONA = register("AZ", "Arizona", Long.valueOf(3L));
    public static final StateType ARKANSAS = register("AR", "Arkansas", Long.valueOf(4L));
    public static final StateType CALIFORNIA = register("CA", "California", Long.valueOf(5L));
    public static final StateType COLORADO = register("CO", "Colorado", Long.valueOf(6L));
    public static final StateType CONNECTICUT = register("CT", "Connecticut", Long.valueOf(7L));
    public static final StateType DELAWARE = register("DE", "Delaware", Long.valueOf(8L));
    public static final StateType DISTRICT_OF_COLUMBIA = register("DC", "District of Columbia", Long.valueOf(9L));
    public static final StateType FLORIDA = register("FL", "Florida", Long.valueOf(10L));
    public static final StateType GEORGIA = register("GA", "Georgia", Long.valueOf(11L));
    public static final StateType HAWAII = register("HI", "Hawaii", Long.valueOf(12L));
    public static final StateType IDAHO = register("ID", "Idaho", Long.valueOf(13L));
    public static final StateType ILLINOIS = register("IL", "Illinois", Long.valueOf(14L));
    public static final StateType INDIANA = register("IN", "Indiana", Long.valueOf(15L));
    public static final StateType IOWA = register("IA", "Iowa", Long.valueOf(16L));
    public static final StateType KANSAS = register("KS", "Kansas", Long.valueOf(17L));
    public static final StateType KENTUCKY = register("KY", "Kentucky", Long.valueOf(18L));
    public static final StateType LOUISIANA = register("LA", "Louisiana", Long.valueOf(19L));
    public static final StateType MAINE = register("ME", "Maine", Long.valueOf(20L));
    public static final StateType MARYLAND = register("MD", "Maryland", Long.valueOf(21L));
    public static final StateType MASSACHUSETTS = register("MA", "Massachusetts", Long.valueOf(22L));
    public static final StateType MICHIGAN = register("MI", "Michigan", Long.valueOf(23L));
    public static final StateType MINNESOTA = register("MN", "Minnesota", Long.valueOf(24L));
    public static final StateType MISSISSIPPI = register("MS", "Mississippi", Long.valueOf(25L));
    public static final StateType MISSOURI = register("MO", "Missouri", Long.valueOf(26L));
    public static final StateType MONTANA = register("MT", "Montana", Long.valueOf(27L));
    public static final StateType NEBRASKA = register("NE", "Nebraska", Long.valueOf(28L));
    public static final StateType NEVADA = register("NV", "Nevada", Long.valueOf(29L));
    public static final StateType NEW_HAMPSHIRE = register("NH", "New Hampshire", Long.valueOf(30L));
    public static final StateType NEW_JERSEY = register("NJ", "New Jersey", Long.valueOf(31L));
    public static final StateType NEW_MEXICO = register("NM", "New Mexico", Long.valueOf(32L));
    public static final StateType NEW_YORK = register("NY", "New York", Long.valueOf(33L));
    public static final StateType NORTH_CAROLINA = register("NC", "North Carolina", Long.valueOf(34L));
    public static final StateType NORTH_DAKOTA = register("ND", "North Dakota", Long.valueOf(35L));
    public static final StateType OHIO = register("OH", "Ohio", Long.valueOf(36L));
    public static final StateType OKLAHOMA = register("OK", "Oklahoma", Long.valueOf(37L));
    public static final StateType OREGON = register("OR", "Oregon", Long.valueOf(38L));
    public static final StateType PENNSYLVANIA = register("PA", "Pennsylvania", Long.valueOf(39L));
    public static final StateType RHODE_ISLAND = register("RI", "Rhode Island", Long.valueOf(40L));
    public static final StateType SOUTH_CAROLINA = register("SC", "South Carolina", Long.valueOf(41L));
    public static final StateType SOUTH_DAKOTA = register("SD", "South Dakota", Long.valueOf(42L));
    public static final StateType TENNESSEE = register("TN", "Tennessee", Long.valueOf(43L));
    public static final StateType TEXAS = register("TX", "Texas", Long.valueOf(44L));
    public static final StateType UTAH = register("UT", "Utah", Long.valueOf(45L));
    public static final StateType VERMONT = register("VT", "Vermont", Long.valueOf(46L));
    public static final StateType VIRGINIA = register("VA", "Virginia", Long.valueOf(47L));
    public static final StateType WASHINGTON = register("WA", "Washington", Long.valueOf(48L));
    public static final StateType WEST_VIRGINIA = register("WV", "West Virginia", Long.valueOf(49L));
    public static final StateType WISCONSIN = register("WI", "Wisconsin", Long.valueOf(50L));
    public static final StateType WYOMING = register("WY", "Wyoming", Long.valueOf(51L));
    public static final StateType PUERTO_RICO = register("PR", "Puerto Rico", Long.valueOf(52L));
    public static final StateType VIRGIN_ISLANDS = register("VI", "U.S. Virgin Islands", Long.valueOf(53L));
    public static final StateType GUAM = register("GU", "Guam", Long.valueOf(54L));

    private StateType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static StateType register(final String code, final String description, final Long databaseId) {
        final StateType type = new StateType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static StateType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static StateType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final StateType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<StateType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
