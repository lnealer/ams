package org.example.am.shared.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.example.am.shared.domain.StateType;

/**
 * Orders states by their two letter code, which is how the state drop-down is labelled.
 */
public class StateComparator implements Comparator<StateType>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final StateType left, final StateType right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return String.valueOf(left.getCode()).compareToIgnoreCase(String.valueOf(right.getCode()));
    }
}
