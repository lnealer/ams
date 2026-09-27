package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.struts2.Action;
import org.example.am.internal.domain.DashboardLayout;
import org.example.am.internal.domain.DashboardPanel;
import org.example.am.internal.domain.comparator.DashboardPanelComparator;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.AssetService;
import org.example.am.internal.service.CustomerService;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The operations dashboard: the landing page after sign-in.
 *
 * <p>Panels are assembled per user rather than fixed, so that someone with only the read-only roles
 * is not shown panels whose actions they could not take.</p>
 */
@Component("DashboardAction")
@Scope("prototype")
public class DashboardAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private static final int ATTENTION_QUEUE_SIZE = 25;
    private static final int RECENT_CUSTOMERS = 10;

    /**
     * Builds the panel set for the signed-in user.
     *
     * <p>A panel is only added when its role is held, so the layout never contains something the
     * user would be refused on clicking.</p>
     */
    private DashboardLayout getDashboardLayoutForUser() {
        final DashboardLayout dashboard = new DashboardLayout();
        dashboard.setUserId(getUserId());

        addPanel(dashboard, "attention", "Assets needing attention",
                "/assetManagement/AttentionQueue.action",
                SecurityRoleType.INT_VIEW_ATTENTION_QUEUE, 0, 0);
        addPanel(dashboard, "recent", "Recently viewed customers",
                "/customer/RecentCustomers.action",
                SecurityRoleType.INT_SEARCH_CUSTOMERS, 0, 1);
        addPanel(dashboard, "installs", "Upcoming installations",
                "/calendar/InstallationTimeSlots.action",
                SecurityRoleType.INT_VIEW_CALENDAR, 1, 0);
        addPanel(dashboard, "queues", "Work queues", "/admin/Queues.action",
                SecurityRoleType.INT_VIEW_QUEUES, 1, 1);

        Collections.sort(dashboard.getPanels(), new DashboardPanelComparator());
        return dashboard;
    }

    private void addPanel(final DashboardLayout dashboard, final String id, final String title,
            final String url, final SecurityRoleType role, final int column, final int position) {
        if (!hasRole(role)) {
            return;
        }
        final DashboardPanel panel = new DashboardPanel();
        panel.setPanelId(id);
        panel.setTitle(title);
        panel.setContentUrl(url);
        panel.setRequiredRole(role);
        panel.setColumn(column);
        panel.setPosition(position);
        dashboard.getPanels().add(panel);
    }

    private DashboardLayout layout;
    private List<Asset> attentionQueue;
    private List<Customer> recentCustomers;

    @Autowired
    private transient AssetService internalAssetService;

    @Autowired
    private transient CustomerService internalCustomerService;

    public String initDashboard() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_DASHBOARD);
        if (denied != null) {
            return denied;
        }
        attentionQueue = hasRole(SecurityRoleType.INT_VIEW_ATTENTION_QUEUE)
                ? internalAssetService.getAttentionQueue(ATTENTION_QUEUE_SIZE)
                : new ArrayList<Asset>();
        recentCustomers = internalCustomerService.getRecentCustomers(getUserId(), RECENT_CUSTOMERS);
        layout = getDashboardLayoutForUser();
        return Action.SUCCESS;
    }

    public String getDashboardLayout() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_DASHBOARD);
        if (denied != null) {
            return denied;
        }
        layout = getDashboardLayoutForUser();
        return Action.SUCCESS;
    }

    public DashboardLayout getLayout() {
        return layout;
    }

    public void setLayout(final DashboardLayout layout) {
        this.layout = layout;
    }

    public List<Asset> getAttentionQueue() {
        return attentionQueue;
    }

    public void setAttentionQueue(final List<Asset> attentionQueue) {
        this.attentionQueue = attentionQueue;
    }

    public List<Customer> getRecentCustomers() {
        return recentCustomers;
    }

    public void setRecentCustomers(final List<Customer> recentCustomers) {
        this.recentCustomers = recentCustomers;
    }
}
