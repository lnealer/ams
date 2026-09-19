package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The on-site visit that brings an asset into service.
 */
public class Installation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long installationId;
    private Long assetId;
    private Long orderId;
    private InstallationStatusType installationStatusType;
    private Date scheduledDate;
    private Timeslot timeslot;
    private Address installationAddress;
    private Contact installationContact;
    private TechLine techLine;
    private String technicianName;
    private String notes;
    private Date completedDate;

    public Long getInstallationId() {
        return installationId;
    }

    public void setInstallationId(final Long installationId) {
        this.installationId = installationId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(final Long assetId) {
        this.assetId = assetId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public InstallationStatusType getInstallationStatusType() {
        return installationStatusType;
    }

    public void setInstallationStatusType(final InstallationStatusType installationStatusType) {
        this.installationStatusType = installationStatusType;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(final Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(final Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Address getInstallationAddress() {
        return installationAddress;
    }

    public void setInstallationAddress(final Address installationAddress) {
        this.installationAddress = installationAddress;
    }

    public Contact getInstallationContact() {
        return installationContact;
    }

    public void setInstallationContact(final Contact installationContact) {
        this.installationContact = installationContact;
    }

    public TechLine getTechLine() {
        return techLine;
    }

    public void setTechLine(final TechLine techLine) {
        this.techLine = techLine;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(final String technicianName) {
        this.technicianName = technicianName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(final Date completedDate) {
        this.completedDate = completedDate;
    }

    public boolean isScheduled() {
        return scheduledDate != null
                && !InstallationStatusType.CANCELLED.equals(installationStatusType);
    }

    public boolean isComplete() {
        return InstallationStatusType.COMPLETED.equals(installationStatusType);
    }

    /**
     * Reschedule is offered right up to the point the technician starts work.
     */
    public boolean isReschedulable() {
        return isScheduled()
                && !isComplete()
                && !InstallationStatusType.IN_PROGRESS.equals(installationStatusType);
    }

}
