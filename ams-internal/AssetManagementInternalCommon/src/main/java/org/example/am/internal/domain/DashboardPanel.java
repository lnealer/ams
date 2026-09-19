package org.example.am.internal.domain;

import java.io.Serializable;

import org.example.am.internal.security.SecurityRoleType;

/**
 * One panel on the operations dashboard.
 *
 * <p>Each panel names the role that gates it, so a user only ever sees panels they could act on.</p>
 */
public class DashboardPanel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String panelId;
    private String title;
    private String contentUrl;
    private int column;
    private int position;
    private boolean visible = true;
    private boolean collapsed;
    private SecurityRoleType requiredRole;

    public String getPanelId() {
        return panelId;
    }

    public void setPanelId(final String panelId) {
        this.panelId = panelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(final String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(final int column) {
        this.column = column;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(final boolean collapsed) {
        this.collapsed = collapsed;
    }

    public SecurityRoleType getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(final SecurityRoleType requiredRole) {
        this.requiredRole = requiredRole;
    }
}
