package org.example.am.internal.web.action;

import java.util.Date;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.dao.StoredProcedureDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Books and rebooks the on-site installation visit.
 */
@Component("InstallationAction")
@Scope("prototype")
public class InstallationAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long timeslotId;
    private Long orderId;
    private Date scheduledDate;
    private boolean booked;

    @Autowired
    private transient StoredProcedureDAO internalStoredProcedureDAO;

    public String bookInstallation() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SCHEDULE_INSTALLATION);
        if (denied != null) {
            return denied;
        }
        if (timeslotId == null || orderId == null) {
            addActionError("Choose a timeslot.");
            return Action.INPUT;
        }
        booked = "OK".equals(internalStoredProcedureDAO.reserveTimeslot(timeslotId.longValue(),
                orderId.longValue(), "INSTALL", scheduledDate, getUserId()));
        if (!booked) {
            addActionError("That timeslot has just been taken. Please choose another.");
            return Action.INPUT;
        }
        addActionMessage("The installation has been booked.");
        return Action.SUCCESS;
    }

    public String cancelInstallation() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_RESCHEDULE_INSTALLATION);
        if (denied != null) {
            return denied;
        }
        if (timeslotId == null || orderId == null) {
            return Action.ERROR;
        }
        internalStoredProcedureDAO.cancelTimeslot(timeslotId.longValue(), orderId.longValue(),
                "INSTALL", getUserId());
        addActionMessage("The installation booking has been released.");
        return Action.SUCCESS;
    }

    public Long getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(final Long timeslotId) {
        this.timeslotId = timeslotId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(final Long orderId) {
        this.orderId = orderId;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(final Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(final boolean booked) {
        this.booked = booked;
    }
}
