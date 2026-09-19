package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Role a contact plays against an order, asset or change request.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class ContactType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, ContactType> VALUES = new LinkedHashMap<String, ContactType>();

    public static final ContactType ORDERING = register("ORDERING", "Ordering Contact", Long.valueOf(1L));
    public static final ContactType SHIPPING = register("SHIPPING", "Shipping Contact", Long.valueOf(2L));
    public static final ContactType INSTALLATION = register("INSTALL", "Installation Contact", Long.valueOf(3L));
    public static final ContactType TECHNICAL = register("TECH", "Technical Contact", Long.valueOf(4L));
    public static final ContactType BILLING = register("BILLING", "Billing Contact", Long.valueOf(5L));
    public static final ContactType END_USER = register("ENDUSER", "End User", Long.valueOf(6L));

    private ContactType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static ContactType register(final String code, final String description, final Long databaseId) {
        final ContactType type = new ContactType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static ContactType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static ContactType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final ContactType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<ContactType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
