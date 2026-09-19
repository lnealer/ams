package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Return Merchandise Authorisation progress.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class RmaStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, RmaStatusType> VALUES = new LinkedHashMap<String, RmaStatusType>();

    public static final RmaStatusType PENDING = register("PENDING", "Pending", Long.valueOf(1L));
    public static final RmaStatusType LABEL_ISSUED = register("LABEL", "Return Label Issued", Long.valueOf(2L));
    public static final RmaStatusType IN_TRANSIT = register("TRANSIT", "In Transit", Long.valueOf(3L));
    public static final RmaStatusType RECEIVED = register("RECEIVED", "Received", Long.valueOf(4L));
    public static final RmaStatusType CLOSED = register("CLOSED", "Closed", Long.valueOf(5L));
    public static final RmaStatusType OVERDUE = register("OVERDUE", "Overdue", Long.valueOf(6L));

    private RmaStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static RmaStatusType register(final String code, final String description, final Long databaseId) {
        final RmaStatusType type = new RmaStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static RmaStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static RmaStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final RmaStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<RmaStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
