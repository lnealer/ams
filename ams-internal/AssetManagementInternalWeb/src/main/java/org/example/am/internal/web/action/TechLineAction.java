package org.example.am.internal.web.action;

import java.util.Date;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.CalendarService;
import org.example.am.internal.service.dao.StoredProcedureDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Books the techline call that runs alongside an installation.
 */
@Component("TechLineAction")
@Scope("prototype")
public class TechLineAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private Long timeslotId;
    private Long orderId;
    private Date scheduledDate;
    private boolean booked;

    @Autowired
    private transient CalendarService internalCalendarService;

    @Autowired
    private transient StoredProcedureDAO internalStoredProcedureDAO;

    public String bookTechline() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SCHEDULE_TECHLINE);
        if (denied != null) {
            return denied;
        }
        if (timeslotId == null || orderId == null) {
            addActionError("Choose a timeslot.");
            return Action.INPUT;
        }
        booked = "OK".equals(internalStoredProcedureDAO.reserveTimeslot(timeslotId.longValue(),
                orderId.longValue(), "TECHLINE", scheduledDate, getUserId()));
        if (!booked) {
            addActionError("That timeslot has just been taken. Please choose another.");
            return Action.INPUT;
        }
        addActionMessage("The techline call has been booked.");
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
