package org.example.am.shared.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.example.am.shared.domain.Asset;

/**
 * Sorts legacy asset tags the way operations staff expect: the alphabetic prefix first, then the
 * numeric suffix compared numerically, so {@code AB-9} sorts before {@code AB-10}.
 *
 * <p>Tags that do not follow the prefix/number shape fall back to a plain case-insensitive
 * comparison and sort after those that do.</p>
 */
public class LegacyAssetTagComparator implements Comparator<Asset>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final Asset left, final Asset right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return compareTags(left.getLegacyAssetTag(), right.getLegacyAssetTag());
    }

    private static int compareTags(final String leftTag, final String rightTag) {
        if (leftTag == null) {
            return rightTag == null ? 0 : 1;
        }
        if (rightTag == null) {
            return -1;
        }

        final String leftPrefix = prefixOf(leftTag);
        final String rightPrefix = prefixOf(rightTag);
        final Long leftNumber = numberOf(leftTag);
        final Long rightNumber = numberOf(rightTag);

        if (leftNumber == null || rightNumber == null) {
            if (leftNumber != rightNumber) {
                return leftNumber == null ? 1 : -1;
            }
            return leftTag.compareToIgnoreCase(rightTag);
        }

        final int byPrefix = leftPrefix.compareToIgnoreCase(rightPrefix);
        if (byPrefix != 0) {
            return byPrefix;
        }
        return leftNumber.compareTo(rightNumber);
    }

    private static String prefixOf(final String tag) {
        int index = 0;
        while (index < tag.length() && !Character.isDigit(tag.charAt(index))) {
            index++;
        }
        return tag.substring(0, index);
    }

    private static Long numberOf(final String tag) {
        int index = 0;
        while (index < tag.length() && !Character.isDigit(tag.charAt(index))) {
            index++;
        }
        if (index == tag.length()) {
            return null;
        }
        final StringBuilder digits = new StringBuilder();
        while (index < tag.length() && Character.isDigit(tag.charAt(index))) {
            digits.append(tag.charAt(index));
            index++;
        }
        if (index != tag.length()) {
            return null;
        }
        try {
            return Long.valueOf(digits.toString());
        } catch (final NumberFormatException tooLong) {
            return null;
        }
    }
}
