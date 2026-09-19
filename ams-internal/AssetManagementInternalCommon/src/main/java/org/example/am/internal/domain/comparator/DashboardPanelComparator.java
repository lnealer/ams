package org.example.am.internal.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.example.am.internal.domain.DashboardPanel;

/** Orders dashboard panels by column, then by their position within it. */
public class DashboardPanelComparator implements Comparator<DashboardPanel>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final DashboardPanel left, final DashboardPanel right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        if (left.getColumn() != right.getColumn()) {
            return left.getColumn() < right.getColumn() ? -1 : 1;
        }
        if (left.getPosition() != right.getPosition()) {
            return left.getPosition() < right.getPosition() ? -1 : 1;
        }
        return String.valueOf(left.getPanelId()).compareToIgnoreCase(String.valueOf(right.getPanelId()));
    }
}
