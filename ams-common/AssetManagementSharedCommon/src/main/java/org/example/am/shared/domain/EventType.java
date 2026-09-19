package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Audit event categories written to AMS_EVENTS.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class EventType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, EventType> VALUES = new LinkedHashMap<String, EventType>();

    public static final EventType ORDER_SUBMITTED = register("ORDSUB", "Order Submitted", Long.valueOf(1L));
    public static final EventType ORDER_CANCELLED = register("ORDCAN", "Order Cancelled", Long.valueOf(2L));
    public static final EventType ASSET_INSTALLED = register("ASTINS", "Asset Installed", Long.valueOf(3L));
    public static final EventType ASSET_DESPATCHED = register("ASTSHIP", "Asset Despatched", Long.valueOf(9L));
    public static final EventType ASSET_DECOMMISSIONED = register("ASTDEC", "Asset Decommissioned", Long.valueOf(4L));
    public static final EventType CONFIG_MODIFIED = register("CFGMOD", "Configuration Modified", Long.valueOf(5L));
    public static final EventType NCR_SUBMITTED = register("NCRSUB", "Network Change Request Submitted", Long.valueOf(6L));
    public static final EventType NCR_CANCELLED = register("NCRCAN", "Network Change Request Cancelled", Long.valueOf(7L));
    public static final EventType RMA_CREATED = register("RMANEW", "RMA Created", Long.valueOf(8L));

    private EventType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static EventType register(final String code, final String description, final Long databaseId) {
        final EventType type = new EventType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static EventType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static EventType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final EventType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<EventType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
