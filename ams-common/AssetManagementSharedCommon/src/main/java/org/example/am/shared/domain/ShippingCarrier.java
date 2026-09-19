package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carriers used for outbound hardware and RMA returns.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class ShippingCarrier extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, ShippingCarrier> VALUES = new LinkedHashMap<String, ShippingCarrier>();

    public static final ShippingCarrier UPS = register("UPS", "United Parcel Service", Long.valueOf(1L));
    public static final ShippingCarrier FEDEX = register("FEDEX", "FedEx", Long.valueOf(2L));
    public static final ShippingCarrier DHL = register("DHL", "DHL", Long.valueOf(3L));
    public static final ShippingCarrier USPS = register("USPS", "United States Postal Service", Long.valueOf(4L));
    public static final ShippingCarrier COURIER = register("COURIER", "Local Courier", Long.valueOf(5L));

    private ShippingCarrier(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static ShippingCarrier register(final String code, final String description, final Long databaseId) {
        final ShippingCarrier type = new ShippingCarrier(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static ShippingCarrier lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static ShippingCarrier lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final ShippingCarrier type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<ShippingCarrier> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
