package org.example.am.shared.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.example.am.shared.domain.LoadableType;

/**
 * Orders any {@link LoadableType} by its description, so drop-downs that are not meant to keep
 * declaration order read alphabetically.
 */
public class LoadableValuesComparator implements Comparator<LoadableType>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final LoadableType left, final LoadableType right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        final String leftDescription = left.getDescription() == null ? "" : left.getDescription();
        final String rightDescription = right.getDescription() == null ? "" : right.getDescription();
        return leftDescription.compareToIgnoreCase(rightDescription);
    }
}
