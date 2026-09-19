package org.example.am.shared.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.example.am.shared.domain.CountryType;

/**
 * Orders countries alphabetically but pins the United States to the top, because the overwhelming
 * majority of shipments are domestic.
 */
public class CountryComparator implements Comparator<CountryType>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final CountryType left, final CountryType right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        final boolean leftIsDefault = CountryType.US.equals(left);
        final boolean rightIsDefault = CountryType.US.equals(right);
        if (leftIsDefault != rightIsDefault) {
            return leftIsDefault ? -1 : 1;
        }
        return String.valueOf(left.getDescription())
                .compareToIgnoreCase(String.valueOf(right.getDescription()));
    }
}
