package org.example.am.shared.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Templates the email service can render.
 *
 * <p>Legacy typesafe-enum: instances are {@code public static final} singletons registered in
 * insertion order so that {@link #values()} can drive a drop-down without any extra sorting.</p>
 */
public final class EmailTemplateType extends LoadableType {

    private static final long serialVersionUID = 1L;

    private static final Map<String, EmailTemplateType> VALUES = new LinkedHashMap<String, EmailTemplateType>();

    public static final EmailTemplateType ORDER_CONFIRMATION = register("ORDCONF", "Order Confirmation", Long.valueOf(1L));
    public static final EmailTemplateType ORDER_CANCELLATION = register("ORDCANCEL", "Order Cancellation", Long.valueOf(2L));
    public static final EmailTemplateType INSTALL_SCHEDULED = register("INSTSCHED", "Installation Scheduled", Long.valueOf(3L));
    public static final EmailTemplateType NCR_CONFIRMATION = register("NCRCONF", "Network Change Request Confirmation", Long.valueOf(4L));
    public static final EmailTemplateType NCR_CANCELLATION = register("NCRCANCEL", "Network Change Request Cancellation", Long.valueOf(5L));
    public static final EmailTemplateType DECOMMISSION_SCHEDULED = register("DECOMSCHED", "Decommission Scheduled", Long.valueOf(6L));
    public static final EmailTemplateType RMA_REMINDER = register("RMAREMIND", "RMA Reminder", Long.valueOf(7L));

    private EmailTemplateType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static EmailTemplateType register(final String code, final String description, final Long databaseId) {
        final EmailTemplateType type = new EmailTemplateType(code, description, databaseId);
        VALUES.put(code, type);
        return type;
    }

    /**
     * @param code database code, may be {@code null}
     * @return the matching instance, or {@code null} when the code is unknown
     */
    public static EmailTemplateType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(code.trim().toUpperCase());
    }

    public static EmailTemplateType lookupByDatabaseId(final Long databaseId) {
        if (databaseId == null) {
            return null;
        }
        for (final EmailTemplateType type : VALUES.values()) {
            if (databaseId.equals(type.getDatabaseId())) {
                return type;
            }
        }
        return null;
    }

    public static Collection<EmailTemplateType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
