package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.network.validation.NetworkUtils;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.SubscriberPc;
import org.example.am.shared.domain.SubscriberPcType;
import org.example.am.shared.service.OrderDefaultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.Action;

/**
 * Step 5 of the ordering flow: the machines that will sit behind the device.
 *
 * <p>Collected now because the LAN keyed on the previous step has to be big enough to hold them,
 * and because any machine needing a fixed address has to have that address reserved out of the
 * DHCP pool before the device is staged. A static address that falls outside the LAN subnet is the
 * single most common reason an install fails on the day, so it is checked here.</p>
 */
@Component("OrderSubscriberPcAction")
@Scope("prototype")
public class OrderSubscriberPcAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    /** How many blank rows the grid offers. More can be added a screenful at a time. */
    private static final int INITIAL_ROWS = 5;
    private static final int ROWS_PER_ADD = 5;
    private static final int MAX_ROWS = 50;

    /** Accepts the three separator styles a MAC address is written in, and none at all. */
    private static final Pattern MAC = Pattern.compile(
            "^([0-9A-Fa-f]{2}([:-]?)){5}[0-9A-Fa-f]{2}$|^([0-9A-Fa-f]{4}\\.){2}[0-9A-Fa-f]{4}$");

    @Autowired
    private transient OrderDefaultsService orderDefaultsService;

    /** What was suggested and why, rendered above the grid. */
    private List<String> suggestionNotes = new ArrayList<String>();

    public String initSubscriberPcs() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        model.ensureSubscriberPcRows(INITIAL_ROWS);

        // Offered once. The grid is mostly the operator's to fill, so the suggestion is deliberately
        // light: a first row named after the device, and an address for anything they mark static.
        if (!model.isSubscriberDefaultsApplied()) {
            suggestionNotes = orderDefaultsService.applySubscriberPcDefaults(model.getSubscriberPcs(),
                    model.getAssetConfiguration(), model.getDeviceNickname());
            model.setSubscriberDefaultsApplied(true);
        }
        storeModel(model);
        return Action.SUCCESS;
    }

    public List<String> getSuggestionNotes() {
        return suggestionNotes;
    }

    /** Adds another screenful of blank rows without leaving the page. */
    public String addSubscriberPcRows() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        final int wanted = model.getSubscriberPcs().size() + ROWS_PER_ADD;
        if (wanted > MAX_ROWS) {
            addActionError("A single order can list at most " + MAX_ROWS + " subscriber machines.");
        } else {
            model.ensureSubscriberPcRows(wanted);
        }
        storeModel(model);
        return Action.SUCCESS;
    }

    public String saveSubscriberPcs() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        // A row only becomes static when the operator ticks the box, which happens after the grid
        // was first drawn - so the allocation has to run again here, before validation complains
        // that a static machine has no address.
        orderDefaultsService.applySubscriberPcDefaults(model.getSubscriberPcs(),
                model.getAssetConfiguration(), null);

        if (!validateRows(model)) {
            model.ensureSubscriberPcRows(INITIAL_ROWS);
            return Action.INPUT;
        }
        model.reachStep(6);
        storeModel(model);
        return Action.SUCCESS;
    }

    public Collection<SubscriberPcType> getSubscriberPcTypeOptions() {
        return SubscriberPcType.values();
    }

    /** @return the rows to render, so the JSP does not have to know how the grid is sized */
    public List<SubscriberPc> getSubscriberPcRows() {
        return getModel().getSubscriberPcs();
    }

    private boolean validateRows(final OrderModel model) {
        final List<SubscriberPc> populated = model.getPopulatedSubscriberPcs();
        if (populated.isEmpty()) {
            addActionError("List at least one machine that will use this device.");
            return false;
        }

        final AssetConfiguration configuration = model.getAssetConfiguration();
        final Set<String> hostNames = new HashSet<String>();
        final Set<String> addresses = new HashSet<String>();
        boolean valid = true;

        for (int index = 0; index < model.getSubscriberPcs().size(); index++) {
            final SubscriberPc pc = model.getSubscriberPcs().get(index);
            if (pc == null || pc.isBlank()) {
                continue;
            }
            final String field = "subscriberPcs[" + index + "]";

            if (isBlank(pc.getHostName())) {
                addFieldErrorAndLog(field + ".hostName", "Row " + (index + 1)
                        + ": a host name is required.");
                valid = false;
            } else if (!hostNames.add(pc.getHostName().trim().toLowerCase())) {
                // Two machines with the same name on one LAN is a name resolution problem the
                // engineer would have to untangle on site.
                addFieldErrorAndLog(field + ".hostName", "Row " + (index + 1)
                        + ": '" + pc.getHostName().trim() + "' is listed more than once.");
                valid = false;
            }

            if (pc.getSubscriberPcType() == null) {
                addFieldErrorAndLog(field + ".subscriberPcType", "Row " + (index + 1)
                        + ": choose what kind of machine this is.");
                valid = false;
            }

            if (pc.getUserCount() != null && pc.getUserCount().intValue() <= 0) {
                addFieldErrorAndLog(field + ".userCount", "Row " + (index + 1)
                        + ": the number of users must be at least one.");
                valid = false;
            }

            if (!isBlank(pc.getMacAddress()) && !MAC.matcher(pc.getMacAddress().trim()).matches()) {
                addFieldErrorAndLog(field + ".macAddress", "Row " + (index + 1)
                        + ": '" + pc.getMacAddress().trim() + "' is not a MAC address.");
                valid = false;
            }

            valid &= validateAddress(pc, index, field, configuration, addresses);
        }
        return valid;
    }

    private boolean validateAddress(final SubscriberPc pc, final int index, final String field,
            final AssetConfiguration configuration, final Set<String> addresses) {
        final boolean hasAddress = !isBlank(pc.getIpAddress());

        if (pc.isStaticAddress() && !hasAddress) {
            addFieldErrorAndLog(field + ".ipAddress", "Row " + (index + 1)
                    + ": a static machine needs the address it should be given.");
            return false;
        }
        if (!hasAddress) {
            return true;
        }

        final String address = pc.getIpAddress().trim();
        if (!NetworkUtils.isValidIpAddress(address)) {
            addFieldErrorAndLog(field + ".ipAddress", "Row " + (index + 1)
                    + ": '" + address + "' is not a valid IPv4 address.");
            return false;
        }
        if (!addresses.add(address)) {
            addFieldErrorAndLog(field + ".ipAddress", "Row " + (index + 1)
                    + ": " + address + " is assigned to more than one machine.");
            return false;
        }

        // Only checkable when the LAN was keyed as static; with DHCP on the LAN side there is no
        // subnet on the order to check against.
        final String lanIp = configuration == null ? null : configuration.getLanIpAddress();
        final String lanMask = configuration == null ? null : configuration.getLanSubnetMask();
        if (isBlank(lanIp) || isBlank(lanMask)) {
            return true;
        }
        if (!NetworkUtils.isSameSubnet(address, lanIp, lanMask)) {
            addFieldErrorAndLog(field + ".ipAddress", "Row " + (index + 1) + ": " + address
                    + " is outside the LAN subnet " + lanIp + " / " + lanMask + ".");
            return false;
        }
        if (NetworkUtils.isNetworkOrBroadcastAddress(address, lanMask)) {
            addFieldErrorAndLog(field + ".ipAddress", "Row " + (index + 1) + ": " + address
                    + " is the network or broadcast address of that subnet.");
            return false;
        }
        if (address.equals(lanIp)) {
            addFieldErrorAndLog(field + ".ipAddress", "Row " + (index + 1) + ": " + address
                    + " is the device's own LAN address.");
            return false;
        }
        return true;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().length() == 0;
    }
}
