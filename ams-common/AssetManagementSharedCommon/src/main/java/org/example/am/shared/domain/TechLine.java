package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * The technical support line engagement booked alongside an installation.
 */
public class TechLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long techLineId;
    private String name;
    private String phoneNumber;
    private String emailAddress;
    private Date scheduledDate;
    private Timeslot timeslot;
    private boolean required;
    private FacilitationCallType facilitationCallType;

    public Long getTechLineId() {
        return techLineId;
    }

    public void setTechLineId(final Long techLineId) {
        this.techLineId = techLineId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public FacilitationCallType getFacilitationCallType() {
        return facilitationCallType;
    }

    public void setFacilitationCallType(final FacilitationCallType facilitationCallType) {
        this.facilitationCallType = facilitationCallType;
    }

    public boolean isBooked() {
        return scheduledDate != null && timeslot != null;
    }

}
