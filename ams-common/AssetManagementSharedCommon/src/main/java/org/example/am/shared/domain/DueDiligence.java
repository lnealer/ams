package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Compliance sign-off captured before an order or change request may be submitted.
 */
public class DueDiligence implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long dueDiligenceId;
    private boolean completed;
    private boolean waived;
    private Date completedDate;
    private String completedBy;
    private String comments;
    private String waiverReason;

    public Long getDueDiligenceId() {
        return dueDiligenceId;
    }

    public void setDueDiligenceId(final Long dueDiligenceId) {
        this.dueDiligenceId = dueDiligenceId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(final boolean completed) {
        this.completed = completed;
    }

    public boolean isWaived() {
        return waived;
    }

    public void setWaived(final boolean waived) {
        this.waived = waived;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(final Date completedDate) {
        this.completedDate = completedDate;
    }

    public String getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(final String completedBy) {
        this.completedBy = completedBy;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public String getWaiverReason() {
        return waiverReason;
    }

    public void setWaiverReason(final String waiverReason) {
        this.waiverReason = waiverReason;
    }

    /**
     * Submission is gated on due diligence either being completed or explicitly waived with a reason.
     */
    public boolean isSatisfied() {
        if (completed) {
            return true;
        }
        return waived && waiverReason != null && waiverReason.trim().length() > 0;
    }

}
