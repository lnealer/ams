package org.example.am.network.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Type B: the managed device owns the LAN outright and serves the site's hosts directly, so it may
 * sit anywhere in the subnet and there is no upstream customer router to point at.
 */
public class LanTypeBValidator extends LanValidator {

    private static final int MAXIMUM_PREFIX_LENGTH = 30;
    private static final long MINIMUM_USABLE_HOSTS = 2L;

    @Override
    public String getLanType() {
        return "B";
    }

    @Override
    protected int getMaximumPrefixLength() {
        return MAXIMUM_PREFIX_LENGTH;
    }

    @Override
    protected boolean isPrivateAddressingRequired() {
        return true;
    }

    @Override
    protected boolean isGatewayRequired() {
        return false;
    }

    @Override
    protected List<String> validateLanType(final String ipAddress, final String subnetMask,
            final String gateway) {
        final List<String> messages = new ArrayList<String>();
        if (NetworkUtils.getUsableHostCount(subnetMask) < MINIMUM_USABLE_HOSTS) {
            messages.add("LAN type B needs at least " + MINIMUM_USABLE_HOSTS
                    + " usable host addresses.");
        }
        return messages;
    }
}
