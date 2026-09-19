package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kind of scheduled call an operations agent facilitates.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class FacilitationCallType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, FacilitationCallType> VALUES = new LinkedHashMap<String, FacilitationCallType>();

    public static final FacilitationCallType TECHLINE = register("TECHLINE", "Techline Call", Long.valueOf(1L));
    public static final FacilitationCallType INSTALLATION = register("INSTALL", "Installation Call", Long.valueOf(2L));
    public static final FacilitationCallType SERVICE = register("SERVICE", "Service Call", Long.valueOf(3L));
    public static final FacilitationCallType NCR = register("NCR", "Network Change Request Call", Long.valueOf(4L));
    public static final FacilitationCallType DECOMMISSION = register("DECOM", "Decommission Call", Long.valueOf(5L));
    /**
     * The despatch window an order ships in. Unlike the others nobody attends it, but it consumes
     * warehouse capacity per region per day in exactly the same way, so it is carried on the same
     * calendar and booked through the same reservation procedure.
     */
    public static final FacilitationCallType SHIPPING = register("SHIP", "Shipping Window", Long.valueOf(6L));

    private FacilitationCallType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static FacilitationCallType register(final String code, final String description, final Long databaseId) {
        final FacilitationCallType type = new FacilitationCallType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static FacilitationCallType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static FacilitationCallType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final FacilitationCallType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<FacilitationCallType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
