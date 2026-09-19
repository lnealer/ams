package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Marks a customer as enrolled in an early adopter programme, which unlocks
 * pre-release asset types in the ordering flow.
 */
public class CustomerEarlyAdopter implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private boolean earlyAdopter;
    private Date enrolledDate;
    private String programName;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public boolean isEarlyAdopter() {
        return earlyAdopter;
    }

    public void setEarlyAdopter(final boolean earlyAdopter) {
        this.earlyAdopter = earlyAdopter;
    }

    public Date getEnrolledDate() {
        return enrolledDate;
    }

    public void setEnrolledDate(final Date enrolledDate) {
        this.enrolledDate = enrolledDate;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(final String programName) {
        this.programName = programName;
    }

}
