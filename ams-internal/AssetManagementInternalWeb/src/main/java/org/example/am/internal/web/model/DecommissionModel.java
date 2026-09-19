package org.example.am.internal.web.model;

import java.io.Serializable;
import java.util.Date;

/**
 * The decommission scheduling form.
 */
public class DecommissionModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private Long assetId;
    private Long decommissionId;
    private Date scheduledDate;
    private Date latestSchedulableDate;
    private String reason;
    private boolean hardwareReturnRequired;
    private String assetTag;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public Long getDecommissionId() {
        return decommissionId;
    }

    public void setDecommissionId(final Long decommissionId) {
        this.decommissionId = decommissionId;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(final Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Date getLatestSchedulableDate() {
        return latestSchedulableDate;
    }

    public void setLatestSchedulableDate(final Date latestSchedulableDate) {
        this.latestSchedulableDate = latestSchedulableDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public boolean isHardwareReturnRequired() {
        return hardwareReturnRequired;
    }

    public void setHardwareReturnRequired(final boolean hardwareReturnRequired) {
        this.hardwareReturnRequired = hardwareReturnRequired;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(final String assetTag) {
        this.assetTag = assetTag;
    }

    /**
     * @return {@code true} when the form has everything the service layer needs; the service still
     *         re-checks the window, because time passes between rendering and submitting
     */
    public boolean isComplete() {
        return assetId != null && scheduledDate != null
                && reason != null && reason.trim().length() > 0;
    }

}
