package org.example.am.shared.domain;

import java.io.Serializable;

/**
 * Recurring weekly window during which disruptive work may be performed.
 */
public class MaintenanceWindow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long maintenanceWindowId;
    private Long assetId;
    private DayType dayType;
    private HourType startHour;
    private HourType endHour;
    private String timeZone;
    private boolean enabled;

    public Long getMaintenanceWindowId() {
        return maintenanceWindowId;
    }

    public void setMaintenanceWindowId(final Long maintenanceWindowId) {
        this.maintenanceWindowId = maintenanceWindowId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public DayType getDayType() {
        return dayType;
    }

    public void setDayType(final DayType dayType) {
        this.dayType = dayType;
    }

    public HourType getStartHour() {
        return startHour;
    }

    public void setStartHour(final HourType startHour) {
        this.startHour = startHour;
    }

    public HourType getEndHour() {
        return endHour;
    }

    public void setEndHour(final HourType endHour) {
        this.endHour = endHour;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getDisplayLabel() {
        if (dayType == null || startHour == null || endHour == null) {
            return "";
        }
        return dayType.getDescription() + " " + startHour.getDescription()
                + " - " + endHour.getDescription()
                + (timeZone == null ? "" : " " + timeZone);
    }

}
