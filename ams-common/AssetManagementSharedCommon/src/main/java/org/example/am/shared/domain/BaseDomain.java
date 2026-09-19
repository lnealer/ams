package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Audit columns shared by every entity that is written through an AMS maintenance screen.
 *
 * <p>{@code currentTime} is not persisted: it is stamped by the service layer from the database
 * clock (or from the QA time-travel override) so that business rules such as cancellation penalty
 * windows evaluate against a single consistent "now" for the whole request.</p>
 */
public abstract class BaseDomain implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date createdDate;
    private Date modifiedDate;
    private Contact createdByContact;
    private Contact modifiedByContact;
    private Date currentTime;

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(final Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Contact getCreatedByContact() {
        return createdByContact;
    }

    public void setCreatedByContact(final Contact createdByContact) {
        this.createdByContact = createdByContact;
    }

    public Contact getModifiedByContact() {
        return modifiedByContact;
    }

    public void setModifiedByContact(final Contact modifiedByContact) {
        this.modifiedByContact = modifiedByContact;
    }

    /**
     * @return the request-scoped "now", falling back to the JVM clock when the service layer has
     *         not stamped one.
     */
    public Date getCurrentTime() {
        return currentTime == null ? new Date() : currentTime;
    }

    public void setCurrentTime(final Date currentTime) {
        this.currentTime = currentTime;
    }
}
