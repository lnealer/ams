package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Document classes published in the help centre.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class HelpCenterDocumentType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, HelpCenterDocumentType> VALUES = new LinkedHashMap<String, HelpCenterDocumentType>();

    public static final HelpCenterDocumentType USER_GUIDE = register("GUIDE", "User Guide", Long.valueOf(1L));
    public static final HelpCenterDocumentType QUICK_REFERENCE = register("QUICKREF", "Quick Reference", Long.valueOf(2L));
    public static final HelpCenterDocumentType RELEASE_NOTE = register("RELNOTE", "Release Note", Long.valueOf(3L));
    public static final HelpCenterDocumentType FAQ = register("FAQ", "Frequently Asked Questions", Long.valueOf(4L));
    public static final HelpCenterDocumentType FORM = register("FORM", "Form", Long.valueOf(5L));

    private HelpCenterDocumentType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static HelpCenterDocumentType register(final String code, final String description, final Long databaseId) {
        final HelpCenterDocumentType type = new HelpCenterDocumentType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static HelpCenterDocumentType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static HelpCenterDocumentType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final HelpCenterDocumentType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<HelpCenterDocumentType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
