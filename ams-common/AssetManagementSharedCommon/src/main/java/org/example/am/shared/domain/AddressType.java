package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Role an address plays against an order or asset.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class AddressType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, AddressType> VALUES = new LinkedHashMap<String, AddressType>();

    public static final AddressType SHIPPING = register("SHIP", "Shipping Address", Long.valueOf(1L));
    public static final AddressType INSTALLATION = register("INSTALL", "Installation Address", Long.valueOf(2L));
    public static final AddressType BILLING = register("BILL", "Billing Address", Long.valueOf(3L));
    public static final AddressType MAILING = register("MAIL", "Mailing Address", Long.valueOf(4L));

    private AddressType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static AddressType register(final String code, final String description, final Long databaseId) {
        final AddressType type = new AddressType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static AddressType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static AddressType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final AddressType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<AddressType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
