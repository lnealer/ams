package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * A scheduled removal of an asset from service.
 */
public class Decommission implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long decommissionId;
    private Long assetId;
    private DecommissionStatusType decommissionStatusType;
    private Date requestedDate;
    private Date scheduledDate;
    private Date completedDate;
    private String reason;
    private Contact requestedByContact;
    private Rma rma;
    private boolean hardwareReturnRequired;

    public Long getDecommissionId() {
        return decommissionId;
    }

    public void setDecommissionId(final Long decommissionId) {
        this.decommissionId = decommissionId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public DecommissionStatusType getDecommissionStatusType() {
        return decommissionStatusType;
    }

    public void setDecommissionStatusType(final DecommissionStatusType decommissionStatusType) {
        this.decommissionStatusType = decommissionStatusType;
    }

    public Date getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(final Date requestedDate) {
        this.requestedDate = requestedDate;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(final Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(final Date completedDate) {
        this.completedDate = completedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public Contact getRequestedByContact() {
        return requestedByContact;
    }

    public void setRequestedByContact(final Contact requestedByContact) {
        this.requestedByContact = requestedByContact;
    }

    public Rma getRma() {
        return rma;
    }

    public void setRma(final Rma rma) {
        this.rma = rma;
    }

    public boolean isHardwareReturnRequired() {
        return hardwareReturnRequired;
    }

    public void setHardwareReturnRequired(final boolean hardwareReturnRequired) {
        this.hardwareReturnRequired = hardwareReturnRequired;
    }

    public boolean isScheduled() {
        return DecommissionStatusType.SCHEDULED.equals(decommissionStatusType) && scheduledDate != null;
    }

    public boolean isCancellable() {
        return DecommissionStatusType.REQUESTED.equals(decommissionStatusType)
                || DecommissionStatusType.SCHEDULED.equals(decommissionStatusType);
    }

}
