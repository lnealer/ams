package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lifecycle state of an order row in AMS_ORDERS.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class OrderStatusType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, OrderStatusType> VALUES = new LinkedHashMap<String, OrderStatusType>();

    public static final OrderStatusType SAVED = register("SAVED", "Saved For Later", Long.valueOf(1L));
    public static final OrderStatusType SUBMITTED = register("SUBMITTED", "Submitted", Long.valueOf(2L));
    public static final OrderStatusType IN_FULFILLMENT = register("FULFILL", "In Fulfillment", Long.valueOf(3L));
    public static final OrderStatusType SHIPPED = register("SHIPPED", "Shipped", Long.valueOf(4L));
    public static final OrderStatusType SCHEDULED = register("SCHEDULED", "Installation Scheduled", Long.valueOf(5L));
    public static final OrderStatusType INSTALLED = register("INSTALLED", "Installed", Long.valueOf(6L));
    public static final OrderStatusType COMPLETED = register("COMPLETED", "Completed", Long.valueOf(7L));
    public static final OrderStatusType CANCELLED = register("CANCELLED", "Cancelled", Long.valueOf(8L));
    public static final OrderStatusType ON_HOLD = register("ONHOLD", "On Hold", Long.valueOf(9L));

    private OrderStatusType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static OrderStatusType register(final String code, final String description, final Long databaseId) {
        final OrderStatusType type = new OrderStatusType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static OrderStatusType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static OrderStatusType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final OrderStatusType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<OrderStatusType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
