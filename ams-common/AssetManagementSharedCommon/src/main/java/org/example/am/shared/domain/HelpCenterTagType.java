package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tags used to index help centre content.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class HelpCenterTagType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, HelpCenterTagType> VALUES = new LinkedHashMap<String, HelpCenterTagType>();

    public static final HelpCenterTagType ORDERING = register("ORDERING", "Ordering", Long.valueOf(1L));
    public static final HelpCenterTagType INSTALLATION = register("INSTALL", "Installation", Long.valueOf(2L));
    public static final HelpCenterTagType CONFIGURATION = register("CONFIG", "Configuration", Long.valueOf(3L));
    public static final HelpCenterTagType DECOMMISSION = register("DECOM", "Decommission", Long.valueOf(4L));
    public static final HelpCenterTagType BILLING = register("BILLING", "Billing", Long.valueOf(5L));
    public static final HelpCenterTagType SECURITY = register("SECURITY", "Security", Long.valueOf(6L));

    private HelpCenterTagType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static HelpCenterTagType register(final String code, final String description, final Long databaseId) {
        final HelpCenterTagType type = new HelpCenterTagType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static HelpCenterTagType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static HelpCenterTagType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final HelpCenterTagType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<HelpCenterTagType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
