package org.example.am.internal.web.action;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.shared.domain.DayType;
import org.example.am.shared.domain.HourType;
import org.example.am.shared.domain.MaintenanceWindow;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.Action;

/**
 * Step 3 of the ordering flow: what to call the device, and when it may be disturbed.
 *
 * <p>The nickname is the customer's own label - "Front desk router", "Warehouse AP" - and is what
 * they will quote on the phone; the asset tag is ours and means nothing to them. The maintenance
 * window is the recurring slot when firmware pushes and reboots are allowed, and getting it wrong
 * means rebooting a shop's till system at lunchtime.</p>
 */
@Component("OrderDeviceAction")
@Scope("prototype")
public class OrderDeviceAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    private static final int MAX_NICKNAME_LENGTH = 60;

    /**
     * The zones the sites this system serves actually sit in. A free-text zone would let a
     * mistyped one through, and the window would then be applied in UTC.
     */
    private static final Map<String, String> TIME_ZONES = new LinkedHashMap<String, String>();

    static {
        TIME_ZONES.put("America/New_York", "Eastern");
        TIME_ZONES.put("America/Chicago", "Central");
        TIME_ZONES.put("America/Denver", "Mountain");
        TIME_ZONES.put("America/Phoenix", "Mountain (no DST)");
        TIME_ZONES.put("America/Los_Angeles", "Pacific");
        TIME_ZONES.put("America/Anchorage", "Alaska");
        TIME_ZONES.put("Pacific/Honolulu", "Hawaii");
    }

    /** Renders the device form, defaulting the window to the small hours on a Sunday. */
    public String initDevice() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        applyDefaults(model);
        storeModel(model);
        return Action.SUCCESS;
    }

    public String saveDevice() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        boolean valid = validateNickname(model);
        valid &= validateWindow(model.getMaintenanceWindow());
        if (!valid) {
            return Action.INPUT;
        }
        model.reachStep(4);
        storeModel(model);
        return Action.SUCCESS;
    }

    public Collection<DayType> getDayOptions() {
        return DayType.values();
    }

    public Collection<HourType> getHourOptions() {
        return HourType.values();
    }

    public Map<String, String> getTimeZoneOptions() {
        return TIME_ZONES;
    }

    private static void applyDefaults(final OrderModel model) {
        final MaintenanceWindow window = model.getMaintenanceWindow();
        if (window.getDayType() == null) {
            // Sunday 01:00-05:00: the least disruptive slot for a shop, and the one operations
            // would otherwise have to ask for on every order.
            window.setDayType(DayType.SUNDAY);
            window.setStartHour(HourType.lookup("0100"));
            window.setEndHour(HourType.lookup("0500"));
            window.setEnabled(true);
        }
        if (window.getTimeZone() == null) {
            final String local = TimeZone.getDefault().getID();
            window.setTimeZone(TIME_ZONES.containsKey(local) ? local : "America/New_York");
        }
    }

    private boolean validateNickname(final OrderModel model) {
        final String nickname = model.getDeviceNickname();
        if (nickname == null || nickname.trim().length() == 0) {
            addFieldErrorAndLog("deviceNickname", "Give the device a nickname.");
            return false;
        }
        if (nickname.trim().length() > MAX_NICKNAME_LENGTH) {
            addFieldErrorAndLog("deviceNickname",
                    "The nickname must be " + MAX_NICKNAME_LENGTH + " characters or fewer.");
            return false;
        }
        model.setDeviceNickname(nickname.trim());
        return true;
    }

    private boolean validateWindow(final MaintenanceWindow window) {
        if (window == null || window.getDayType() == null) {
            addFieldErrorAndLog("maintenanceWindow.dayType", "Choose a maintenance day.");
            return false;
        }
        boolean valid = true;
        if (window.getStartHour() == null) {
            addFieldErrorAndLog("maintenanceWindow.startHour", "Choose a start hour.");
            valid = false;
        }
        if (window.getEndHour() == null) {
            addFieldErrorAndLog("maintenanceWindow.endHour", "Choose an end hour.");
            valid = false;
        }
        if (!valid) {
            return false;
        }
        // Compared as codes because they are zero padded 24 hour strings, so string order is time
        // order. Equal is rejected as well as backwards: a zero length window silently means
        // "never", and the customer would wonder why no firmware update ever lands.
        if (window.getStartHour().getCode().compareTo(window.getEndHour().getCode()) >= 0) {
            addFieldErrorAndLog("maintenanceWindow.endHour",
                    "The window must end after it starts.");
            return false;
        }
        if (window.getTimeZone() == null || !TIME_ZONES.containsKey(window.getTimeZone())) {
            addFieldErrorAndLog("maintenanceWindow.timeZone", "Choose a time zone.");
            return false;
        }
        window.setEnabled(true);
        return true;
    }
}
