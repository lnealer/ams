package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Flow whose in-progress form state is parked in AMS_SAVE_FOR_LATER.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class SaveForLaterType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, SaveForLaterType> VALUES = new LinkedHashMap<String, SaveForLaterType>();

    public static final SaveForLaterType ORDER = register("ORDER", "Order", Long.valueOf(1L));
    public static final SaveForLaterType NETWORK_CHANGE_REQUEST = register("NCR", "Network Change Request", Long.valueOf(2L));
    public static final SaveForLaterType MODIFY_CONFIGURATION = register("MODCFG", "Modify Configuration", Long.valueOf(3L));
    public static final SaveForLaterType DECOMMISSION = register("DECOM", "Decommission", Long.valueOf(4L));

    private SaveForLaterType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static SaveForLaterType register(final String code, final String description, final Long databaseId) {
        final SaveForLaterType type = new SaveForLaterType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static SaveForLaterType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static SaveForLaterType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final SaveForLaterType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<SaveForLaterType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
