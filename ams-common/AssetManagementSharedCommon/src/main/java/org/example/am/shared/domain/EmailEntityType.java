package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entity an outbound email is anchored to.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class EmailEntityType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, EmailEntityType> VALUES = new LinkedHashMap<String, EmailEntityType>();

    public static final EmailEntityType ORDER = register("ORDER", "Order", Long.valueOf(1L));
    public static final EmailEntityType ASSET = register("ASSET", "Asset", Long.valueOf(2L));
    public static final EmailEntityType NETWORK_CHANGE_REQUEST = register("NCR", "Network Change Request", Long.valueOf(3L));
    public static final EmailEntityType RMA = register("RMA", "RMA", Long.valueOf(4L));
    public static final EmailEntityType DECOMMISSION = register("DECOM", "Decommission", Long.valueOf(5L));

    private EmailEntityType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static EmailEntityType register(final String code, final String description, final Long databaseId) {
        final EmailEntityType type = new EmailEntityType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static EmailEntityType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static EmailEntityType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final EmailEntityType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<EmailEntityType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
