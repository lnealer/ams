package org.example.am.internal.security;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.example.am.shared.domain.LoadableType;

/**
 * The internal application's permissions.
 *
 * <p>Deliberately fine grained: one role per action rather than a handful of job-title roles, so
 * that an operations team can be given exactly the actions it is signed off for without the
 * application having to know what their team is called. The codes are what the container sees, so
 * {@code request.isUserInRole(role.getCode())} is the whole authorisation check.</p>
 *
 * <p>The roles arrive on the request as LDAP group memberships, mapped to these codes by
 * {@code AmsUserDetailsDAOImpl} from a database table, so a new group can be granted an existing
 * role without a redeploy.</p>
 */
public final class SecurityRoleType extends LoadableType {

    private static final long serialVersionUID = 1L;

    /** Prefix Spring Security expects on a granted authority. */
    public static final String ROLE_PREFIX = "ROLE_";

    /** The authority every authenticated user holds, checked by the catch-all URL rule. */
    public static final String ROLE_USER = "ROLE_USER";

    private static final Map<String, SecurityRoleType> VALUES =
            new LinkedHashMap<String, SecurityRoleType>();

    public static final SecurityRoleType INT_SEARCH_ASSETS = register("INT_SEARCH_ASSETS", "Search assets", Long.valueOf(1L));
    public static final SecurityRoleType INT_VIEW_ASSET = register("INT_VIEW_ASSET", "View asset detail", Long.valueOf(2L));
    public static final SecurityRoleType INT_VIEW_ASSET_HISTORY = register("INT_VIEW_ASSET_HISTORY", "View asset history", Long.valueOf(3L));
    public static final SecurityRoleType INT_VIEW_ASSET_CONFIG_HISTORY = register("INT_VIEW_ASSET_CONFIG_HISTORY", "View asset configuration history", Long.valueOf(4L));
    public static final SecurityRoleType INT_COMPARE_CONFIG = register("INT_COMPARE_CONFIG", "Compare stored and device configuration", Long.valueOf(5L));
    public static final SecurityRoleType INT_RESOLVE_CONFIG_MISMATCH = register("INT_RESOLVE_CONFIG_MISMATCH", "Resolve a configuration mismatch", Long.valueOf(6L));
    public static final SecurityRoleType INT_MODIFY_CONFIG = register("INT_MODIFY_CONFIG", "Modify an asset configuration", Long.valueOf(7L));
    public static final SecurityRoleType INT_UPDATE_INSTALL_ADDRESS = register("INT_UPDATE_INSTALL_ADDRESS", "Update an installation address", Long.valueOf(8L));
    public static final SecurityRoleType INT_MOVE_ASSET = register("INT_MOVE_ASSET", "Move an asset", Long.valueOf(9L));
    public static final SecurityRoleType INT_DECOMMISSION_ASSET = register("INT_DECOMMISSION_ASSET", "Decommission an asset", Long.valueOf(10L));
    public static final SecurityRoleType INT_SCHEDULE_DECOMMISSION = register("INT_SCHEDULE_DECOMMISSION", "Schedule a decommission", Long.valueOf(11L));
    public static final SecurityRoleType INT_CANCEL_DECOMMISSION = register("INT_CANCEL_DECOMMISSION", "Cancel a scheduled decommission", Long.valueOf(12L));
    public static final SecurityRoleType INT_CREATE_RMA = register("INT_CREATE_RMA", "Create an RMA", Long.valueOf(13L));
    public static final SecurityRoleType INT_VIEW_RMA = register("INT_VIEW_RMA", "View RMAs", Long.valueOf(14L));
    public static final SecurityRoleType INT_UPDATE_RMA = register("INT_UPDATE_RMA", "Update an RMA", Long.valueOf(15L));
    public static final SecurityRoleType INT_SEARCH_CUSTOMERS = register("INT_SEARCH_CUSTOMERS", "Search customers", Long.valueOf(16L));
    public static final SecurityRoleType INT_VIEW_CUSTOMER = register("INT_VIEW_CUSTOMER", "View customer detail", Long.valueOf(17L));
    public static final SecurityRoleType INT_CUSTOMER_ADMIN = register("INT_CUSTOMER_ADMIN", "Administer a customer", Long.valueOf(18L));
    public static final SecurityRoleType INT_ENABLE_ORDERING = register("INT_ENABLE_ORDERING", "Enable or disable customer ordering", Long.valueOf(19L));
    public static final SecurityRoleType INT_VIEW_CONTACTS = register("INT_VIEW_CONTACTS", "View customer contacts", Long.valueOf(20L));
    public static final SecurityRoleType INT_EDIT_CONTACTS = register("INT_EDIT_CONTACTS", "Edit customer contacts", Long.valueOf(21L));
    public static final SecurityRoleType INT_VIEW_SERVICES = register("INT_VIEW_SERVICES", "View customer services", Long.valueOf(22L));
    public static final SecurityRoleType INT_CREATE_ORDER = register("INT_CREATE_ORDER", "Create an order", Long.valueOf(23L));
    public static final SecurityRoleType INT_SUBMIT_ORDER = register("INT_SUBMIT_ORDER", "Submit an order", Long.valueOf(24L));
    public static final SecurityRoleType INT_CANCEL_ORDER = register("INT_CANCEL_ORDER", "Cancel an order", Long.valueOf(25L));
    public static final SecurityRoleType INT_SAVE_ORDER_FOR_LATER = register("INT_SAVE_ORDER_FOR_LATER", "Save an order for later", Long.valueOf(26L));
    public static final SecurityRoleType INT_VIEW_ORDER = register("INT_VIEW_ORDER", "View an order", Long.valueOf(27L));
    public static final SecurityRoleType INT_SELECT_ASSET = register("INT_SELECT_ASSET", "Select an asset for an order", Long.valueOf(28L));
    public static final SecurityRoleType INT_EMERGENCY_REPLACEMENT = register("INT_EMERGENCY_REPLACEMENT", "Raise an emergency replacement", Long.valueOf(29L));
    public static final SecurityRoleType INT_ENABLE_EMERGENCY_REPLACEMENT = register("INT_ENABLE_EMERGENCY_REPLACEMENT", "Enable emergency replacement for an asset", Long.valueOf(30L));
    public static final SecurityRoleType INT_OVERRIDE_DUE_DILIGENCE = register("INT_OVERRIDE_DUE_DILIGENCE", "Waive due diligence", Long.valueOf(31L));
    public static final SecurityRoleType INT_CREATE_NCR = register("INT_CREATE_NCR", "Create a network change request", Long.valueOf(32L));
    public static final SecurityRoleType INT_SUBMIT_NCR = register("INT_SUBMIT_NCR", "Submit a network change request", Long.valueOf(33L));
    public static final SecurityRoleType INT_CANCEL_NCR = register("INT_CANCEL_NCR", "Cancel a network change request", Long.valueOf(34L));
    public static final SecurityRoleType INT_RESCHEDULE_NCR = register("INT_RESCHEDULE_NCR", "Reschedule a network change request", Long.valueOf(35L));
    public static final SecurityRoleType INT_VIEW_NCR = register("INT_VIEW_NCR", "View a network change request", Long.valueOf(36L));
    public static final SecurityRoleType INT_VIEW_NCR_HISTORY = register("INT_VIEW_NCR_HISTORY", "View network change request history", Long.valueOf(37L));
    public static final SecurityRoleType INT_VIEW_CALENDAR = register("INT_VIEW_CALENDAR", "View the scheduling calendars", Long.valueOf(38L));
    public static final SecurityRoleType INT_SCHEDULE_TECHLINE = register("INT_SCHEDULE_TECHLINE", "Book a techline call", Long.valueOf(39L));
    public static final SecurityRoleType INT_SCHEDULE_INSTALLATION = register("INT_SCHEDULE_INSTALLATION", "Book an installation visit", Long.valueOf(40L));
    public static final SecurityRoleType INT_DESPATCH_ORDER = register("INT_DESPATCH_ORDER", "Despatch an order and create the asset", Long.valueOf(52L));
    public static final SecurityRoleType INT_COMPLETE_INSTALLATION = register("INT_COMPLETE_INSTALLATION", "Record an installation as completed", Long.valueOf(53L));
    public static final SecurityRoleType INT_RESCHEDULE_INSTALLATION = register("INT_RESCHEDULE_INSTALLATION", "Reschedule an installation visit", Long.valueOf(41L));
    public static final SecurityRoleType INT_VIEW_DASHBOARD = register("INT_VIEW_DASHBOARD", "View the operations dashboard", Long.valueOf(42L));
    public static final SecurityRoleType INT_VIEW_ATTENTION_QUEUE = register("INT_VIEW_ATTENTION_QUEUE", "View the needs attention queue", Long.valueOf(43L));
    public static final SecurityRoleType INT_CLEAR_ATTENTION_FLAG = register("INT_CLEAR_ATTENTION_FLAG", "Clear a needs attention flag", Long.valueOf(44L));
    public static final SecurityRoleType INT_ADMIN_UTILITIES = register("INT_ADMIN_UTILITIES", "Use the administration utilities", Long.valueOf(45L));
    public static final SecurityRoleType INT_VIEW_QUEUES = register("INT_VIEW_QUEUES", "View the asynchronous work queues", Long.valueOf(46L));
    public static final SecurityRoleType INT_REQUEUE_FAILED_WORK = register("INT_REQUEUE_FAILED_WORK", "Requeue failed work", Long.valueOf(47L));
    public static final SecurityRoleType INT_EDIT_PROPERTIES = register("INT_EDIT_PROPERTIES", "Edit runtime properties", Long.valueOf(48L));
    public static final SecurityRoleType INT_VIEW_TERMS = register("INT_VIEW_TERMS", "View terms and conditions", Long.valueOf(49L));
    public static final SecurityRoleType INT_CHANGE_USER = register("INT_CHANGE_USER", "Use the non production impersonation screen", Long.valueOf(50L));
    public static final SecurityRoleType INT_OVERRIDE_CURRENT_TIME = register("INT_OVERRIDE_CURRENT_TIME", "Override the current time in non production", Long.valueOf(51L));

    private SecurityRoleType(final String code, final String description, final Long databaseId) {
        super(code, description, databaseId);
    }

    private static SecurityRoleType register(final String code, final String description,
            final Long databaseId) {
        final SecurityRoleType role = new SecurityRoleType(code, description, databaseId);
        VALUES.put(code, role);
        return role;
    }

    /**
     * @param code role code, with or without the {@link #ROLE_PREFIX}
     * @return the matching role, or {@code null} when the code is not one this application knows
     */
    public static SecurityRoleType lookup(final String code) {
        if (code == null) {
            return null;
        }
        return VALUES.get(stripPrefix(code));
    }

    /**
     * @return the code with any {@link #ROLE_PREFIX} removed, trimmed and upper cased
     */
    public static String stripPrefix(final String code) {
        if (code == null) {
            return null;
        }
        final String normalised = code.trim().toUpperCase();
        return normalised.startsWith(ROLE_PREFIX)
                ? normalised.substring(ROLE_PREFIX.length()) : normalised;
    }

    /** @return this role's code with the Spring Security prefix applied */
    public String getAuthority() {
        return ROLE_PREFIX + getCode();
    }

    public static boolean isKnown(final String code) {
        return lookup(code) != null;
    }

    public static Collection<SecurityRoleType> values() {
        return Collections.unmodifiableCollection(VALUES.values());
    }

    public static Collection<String> codes() {
        return Collections.unmodifiableCollection(VALUES.keySet());
    }
}
