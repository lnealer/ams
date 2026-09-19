package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.Event;
import org.example.am.shared.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Serves the history tabs.
 */
@Component("AjaxEventAction")
@Scope("prototype")
public class AjaxEventAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private List<Event> events;
    private Long entityId;
    private String entityType;

    @Autowired
    private transient RequestService requestService;

    public String listEvents() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_ASSET_HISTORY);
        if (denied != null) {
            return denied;
        }
        if (entityId == null) {
            events = new ArrayList<Event>();
            return Action.SUCCESS;
        }
        events = requestService.getHistory(EmailEntityType.lookup(entityType),
                entityId.longValue());
        return Action.SUCCESS;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(final List<Event> events) {
        this.events = events;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(final Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(final String entityType) {
        this.entityType = entityType;
    }
}
