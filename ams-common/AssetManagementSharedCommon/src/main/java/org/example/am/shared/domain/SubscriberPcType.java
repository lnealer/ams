package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Kind of subscriber machine sitting behind a managed device.
 *
 * <p>Captured when the order is placed because it drives how the LAN is sized and addressed: a
 * site of twenty tills needs a different subnet and DHCP pool from a site of two laptops, and by
 * the time an engineer is on site the configuration has already been built.</p>
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class SubscriberPcType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, SubscriberPcType> VALUES = new LinkedHashMap<String, SubscriberPcType>();

    public static final SubscriberPcType DESKTOP = register("DESKTOP", "Desktop PC", Long.valueOf(1L));
    public static final SubscriberPcType LAPTOP = register("LAPTOP", "Laptop", Long.valueOf(2L));
    public static final SubscriberPcType POS_TERMINAL = register("POS", "Point Of Sale Terminal", Long.valueOf(3L));
    public static final SubscriberPcType KIOSK = register("KIOSK", "Self Service Kiosk", Long.valueOf(4L));
    public static final SubscriberPcType SERVER = register("SERVER", "On Site Server", Long.valueOf(5L));
    public static final SubscriberPcType PRINTER = register("PRINTER", "Network Printer", Long.valueOf(6L));
    public static final SubscriberPcType VOIP_PHONE = register("VOIP", "VoIP Handset", Long.valueOf(7L));
    public static final SubscriberPcType TABLET = register("TABLET", "Tablet", Long.valueOf(8L));

    private SubscriberPcType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static SubscriberPcType register(final String code, final String description,
            final Long databaseId) {
        final SubscriberPcType type = new SubscriberPcType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static SubscriberPcType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static SubscriberPcType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final SubscriberPcType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<SubscriberPcType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
