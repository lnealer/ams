package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CRUD-ish verb recorded against an audited entity.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class EntityActionEnum extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, EntityActionEnum> VALUES = new LinkedHashMap<String, EntityActionEnum>();

    public static final EntityActionEnum CREATE = register("CREATE", "Create", Long.valueOf(1L));
    public static final EntityActionEnum READ = register("READ", "Read", Long.valueOf(2L));
    public static final EntityActionEnum UPDATE = register("UPDATE", "Update", Long.valueOf(3L));
    public static final EntityActionEnum DELETE = register("DELETE", "Delete", Long.valueOf(4L));
    public static final EntityActionEnum CANCEL = register("CANCEL", "Cancel", Long.valueOf(5L));
    public static final EntityActionEnum SUBMIT = register("SUBMIT", "Submit", Long.valueOf(6L));

    private EntityActionEnum(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static EntityActionEnum register(final String code, final String description, final Long databaseId) {
        final EntityActionEnum type = new EntityActionEnum(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static EntityActionEnum lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static EntityActionEnum lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final EntityActionEnum type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<EntityActionEnum> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
