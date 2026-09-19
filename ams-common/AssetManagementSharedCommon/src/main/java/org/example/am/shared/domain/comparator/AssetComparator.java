package org.example.am.shared.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.example.am.shared.domain.Asset;

/**
 * Default asset grid ordering: assets needing attention float to the top, then by display tag.
 */
public class AssetComparator implements Comparator<Asset>, Serializable {

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
        if (left.isNeedsAttention() != right.isNeedsAttention()) {
            return left.isNeedsAttention() ? -1 : 1;
        }
        final String leftTag = left.getDisplayTag() == null ? "" : left.getDisplayTag();
        final String rightTag = right.getDisplayTag() == null ? "" : right.getDisplayTag();
        final int byTag = leftTag.compareToIgnoreCase(rightTag);
        if (byTag != 0) {
            return byTag;
        }
        return compareIds(left.getAssetId(), right.getAssetId());
    }

    private static int compareIds(final Long left, final Long right) {
        if (left == null) {
            return right == null ? 0 : 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }
}
