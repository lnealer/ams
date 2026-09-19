package org.example.am.internal.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The operations dashboard's arrangement of panels for one user.
 *
 * <p>Internal-only: the customer facing application has no dashboard, so this lives here rather
 * than in the shared domain module.</p>
 */
public class DashboardLayout implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private List<DashboardPanel> panels = new ArrayList<DashboardPanel>();
    private int columnCount = 2;

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public List<DashboardPanel> getPanels() {
        return panels;
    }

    public void setPanels(final List<DashboardPanel> panels) {
        this.panels = panels == null ? new ArrayList<DashboardPanel>() : panels;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(final int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * @param column zero based column index
     * @return the visible panels assigned to that column, in display order
     */
    public List<DashboardPanel> getPanelsForColumn(final int column) {
        final List<DashboardPanel> inColumn = new ArrayList<DashboardPanel>();
        for (final DashboardPanel panel : panels) {
            if (panel.isVisible() && panel.getColumn() == column) {
                inColumn.add(panel);
            }
        }
        return inColumn;
    }
}
