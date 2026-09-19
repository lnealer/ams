package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Link targets rendered on help centre pages.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class HelpCenterLinkType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, HelpCenterLinkType> VALUES = new LinkedHashMap<String, HelpCenterLinkType>();

    public static final HelpCenterLinkType INTERNAL = register("INTERNAL", "Internal Link", Long.valueOf(1L));
    public static final HelpCenterLinkType EXTERNAL = register("EXTERNAL", "External Link", Long.valueOf(2L));
    public static final HelpCenterLinkType DOCUMENT = register("DOCUMENT", "Document Download", Long.valueOf(3L));
    public static final HelpCenterLinkType VIDEO = register("VIDEO", "Video", Long.valueOf(4L));

    private HelpCenterLinkType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static HelpCenterLinkType register(final String code, final String description, final Long databaseId) {
        final HelpCenterLinkType type = new HelpCenterLinkType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static HelpCenterLinkType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static HelpCenterLinkType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final HelpCenterLinkType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<HelpCenterLinkType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
