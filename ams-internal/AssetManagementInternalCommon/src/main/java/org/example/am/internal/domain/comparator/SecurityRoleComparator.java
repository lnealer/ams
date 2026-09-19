package org.example.am.internal.domain.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.example.am.internal.security.SecurityRoleType;

/** Orders roles by description, for the impersonation screen's role picker. */
public class SecurityRoleComparator implements Comparator<SecurityRoleType>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final SecurityRoleType left, final SecurityRoleType right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return String.valueOf(left.getDescription())
                .compareToIgnoreCase(String.valueOf(right.getDescription()));
    }
}
