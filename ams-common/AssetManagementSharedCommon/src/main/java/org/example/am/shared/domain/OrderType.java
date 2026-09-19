package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Business reason an order was raised; drives lead time and fulfilment routing.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class OrderType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, OrderType> VALUES = new LinkedHashMap<String, OrderType>();

    public static final OrderType NEW_INSTALL = register("NEW", "New Installation", Long.valueOf(1L));
    public static final OrderType REPLACEMENT = register("REPLACE", "Replacement", Long.valueOf(2L));
    public static final OrderType EMERGENCY_REPLACEMENT = register("EMERGREPL", "Emergency Replacement", Long.valueOf(3L));
    public static final OrderType MIGRATION = register("MIGRATE", "Migration", Long.valueOf(4L));
    public static final OrderType MOVE = register("MOVE", "Move", Long.valueOf(5L));
    public static final OrderType UPGRADE = register("UPGRADE", "Upgrade", Long.valueOf(6L));

    private OrderType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static OrderType register(final String code, final String description, final Long databaseId) {
        final OrderType type = new OrderType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static OrderType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static OrderType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final OrderType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<OrderType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
