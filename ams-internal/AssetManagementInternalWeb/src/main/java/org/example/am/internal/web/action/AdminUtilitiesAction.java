package org.example.am.internal.web.action;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.shared.domain.ETLStatus;
import org.example.am.shared.service.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The administration utilities screen: queue depths and the state of the nightly load.
 */
@Component("AdminUtilitiesAction")
@Scope("prototype")
public class AdminUtilitiesAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private static final String CONFIG_PUSH_QUEUE = "CONFIG_PUSH";
    private static final String DEVICE_POLL_JOB = "DEVICE_POLL";
    private static final List<String> MONITORED_QUEUES = Arrays.asList(CONFIG_PUSH_QUEUE, "EMAIL");

    /** Anything unprocessed after this long is worth a human looking at. */
    private static final int STUCK_AFTER_MINUTES = 30;

    private Map<String, Integer> queueDepths;
    private ETLStatus lastEtlStatus;
    private List<String> stuckEntries;
    private Map<String, String> properties;

    @Autowired
    private transient AdministrationService administrationService;

    public String initAdminUtilities() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_ADMIN_UTILITIES);
        if (denied != null) {
            return denied;
        }
        queueDepths = administrationService.getQueueDepths(MONITORED_QUEUES);
        lastEtlStatus = administrationService.getLastEtlStatus(DEVICE_POLL_JOB);
        stuckEntries = administrationService.getStuckEntries(CONFIG_PUSH_QUEUE, STUCK_AFTER_MINUTES);
        properties = administrationService.getAllProperties();
        return Action.SUCCESS;
    }

    public Map<String, Integer> getQueueDepths() {
        return queueDepths;
    }

    public void setQueueDepths(final Map<String, Integer> queueDepths) {
        this.queueDepths = queueDepths;
    }

    public ETLStatus getLastEtlStatus() {
        return lastEtlStatus;
    }

    public void setLastEtlStatus(final ETLStatus lastEtlStatus) {
        this.lastEtlStatus = lastEtlStatus;
    }

    public List<String> getStuckEntries() {
        return stuckEntries;
    }

    public void setStuckEntries(final List<String> stuckEntries) {
        this.stuckEntries = stuckEntries;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }
}
