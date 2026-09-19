package org.example.am.shared.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.example.am.shared.domain.Event;

/**
 * Newest event first, which is the order the history tabs render.
 */
public class EventComparator implements Comparator<Event>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final Event left, final Event right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        if (left.getEventDate() == null) {
            return right.getEventDate() == null ? 0 : 1;
        }
        if (right.getEventDate() == null) {
            return -1;
        }
        return right.getEventDate().compareTo(left.getEventDate());
    }
}
