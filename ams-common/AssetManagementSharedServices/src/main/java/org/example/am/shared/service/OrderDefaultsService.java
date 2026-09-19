package org.example.am.shared.service;

import java.util.List;

import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.SubscriberPc;

/**
 * Suggested values for the two longest screens in the ordering flow.
 *
 * <p>Everything suggested here is either carried forward from something the customer already has,
 * computed from a value on the order, or read from {@code AMS_PROPERTIES}. Nothing is invented,
 * and the distinction matters most on the WAN side: {@code WanValidator} rejects RFC 1918 space and
 * every documentation range, so a made up WAN address is necessarily a real address belonging to
 * somebody else - and it would pass validation and ship on a staged device. A blank field an
 * operator has to fill from the carrier's assignment is the correct outcome when AMS has nothing
 * real to offer.</p>
 *
 * <p>Suggestions only ever fill blanks. A field the operator has typed into is never overwritten,
 * so re-entering the screen cannot undo their work.</p>
 */
public interface OrderDefaultsService {

    /**
     * Fills the blank fields of a new configuration.
     *
     * @param configuration the in-progress configuration, modified in place
     * @param customerId    whose estate the values are drawn from
     * @return one line per field filled, saying where the value came from, for display on the
     *         screen; empty when there was nothing to suggest
     */
    List<String> applyConfigurationDefaults(AssetConfiguration configuration, long customerId);

    /**
     * Fills blank rows of the subscriber grid, and assigns addresses to rows marked static.
     *
     * @param rows          the grid, modified in place; populated rows are left alone
     * @param configuration supplies the LAN the addresses are allocated from
     * @param hostNameStem  what generated host names are built from, usually the device nickname
     * @return one line per suggestion made, for display on the screen
     */
    List<String> applySubscriberPcDefaults(List<SubscriberPc> rows, AssetConfiguration configuration,
            String hostNameStem);
}
