package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.struts2.Action;
import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.web.model.OrderModel;
import org.example.am.network.validation.LanTypeAValidator;
import org.example.am.network.validation.LanTypeBValidator;
import org.example.am.network.validation.LanTypeCValidator;
import org.example.am.network.validation.LanValidator;
import org.example.am.network.validation.WanValidator;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.AssetConfigurationStatusType;
import org.example.am.shared.domain.AssetConfigurationType;
import org.example.am.shared.domain.NetworkConfigurationType;
import org.example.am.shared.service.OrderDefaultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Step 4 of the ordering flow: the external configuration, WAN and LAN.
 *
 * <p>Both sides are keyed here rather than left to the engineer on the day, because the device is
 * staged in the warehouse from this configuration: it arrives on site already addressed. That is
 * also why the validators run here and not just at install time - a transposed digit found on site
 * is a wasted visit.</p>
 *
 * <p>DHCP and PPPoE WAN configurations are the exception: the carrier assigns the address at
 * connection time, so there is nothing to key and nothing to validate.</p>
 */
@Component("OrderConfigurationAction")
@Scope("prototype")
public class OrderConfigurationAction extends OrderBaseAction {

    private static final long serialVersionUID = 1L;

    private static final WanValidator WAN_VALIDATOR = new WanValidator();

    /** Which LAN validator applies is decided by the configuration type the user picks. */
    private static final LanValidator LAN_TYPE_A = new LanTypeAValidator();
    private static final LanValidator LAN_TYPE_B = new LanTypeBValidator();
    private static final LanValidator LAN_TYPE_C = new LanTypeCValidator();

    @Autowired
    private transient OrderDefaultsService orderDefaultsService;

    /** What was suggested and why, rendered above the form so the operator knows to check it. */
    private List<String> suggestionNotes = new ArrayList<String>();

    public String initConfiguration() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        final AssetConfiguration configuration = model.getAssetConfiguration();
        if (configuration.getNetworkConfigurationType() == null) {
            configuration.setNetworkConfigurationType(NetworkConfigurationType.STATIC);
        }
        if (configuration.getAssetConfigurationType() == null) {
            configuration.setAssetConfigurationType(AssetConfigurationType.LAN_TYPE_A);
        }

        // Offered once, on first arrival. The service fills blanks only, but applying it on every
        // visit would still refill a field the operator had deliberately cleared on the way back.
        if (!model.isConfigurationDefaultsApplied() && model.getCustomerId() != null) {
            suggestionNotes = orderDefaultsService.applyConfigurationDefaults(configuration,
                    model.getCustomerId().longValue());
            model.setConfigurationDefaultsApplied(true);
        }
        storeModel(model);
        return Action.SUCCESS;
    }

    public List<String> getSuggestionNotes() {
        return suggestionNotes;
    }

    public String saveConfiguration() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_CREATE_ORDER);
        if (denied != null) {
            return denied;
        }
        final OrderModel model = getModel();
        if (!validateCustomerCanOrder(model)) {
            return Action.INPUT;
        }
        final AssetConfiguration configuration = model.getAssetConfiguration();

        boolean valid = validateWan(configuration);
        valid &= validateLan(configuration);
        valid &= validateBandwidth(configuration);
        if (!valid) {
            return Action.INPUT;
        }

        // The revision the warehouse will stage from has not been applied to anything yet, and
        // will not be until the engineer confirms it on site.
        configuration.setAssetConfigurationStatusType(AssetConfigurationStatusType.PENDING);
        model.reachStep(5);
        storeModel(model);
        return Action.SUCCESS;
    }

    public Collection<NetworkConfigurationType> getNetworkConfigurationOptions() {
        return NetworkConfigurationType.values();
    }

    public Collection<AssetConfigurationType> getConfigurationTypeOptions() {
        return AssetConfigurationType.values();
    }

    /**
     * @return {@code true} when the carrier assigns the WAN address, so there is nothing to key
     */
    private static boolean isCarrierAssigned(final AssetConfiguration configuration) {
        final NetworkConfigurationType type = configuration.getNetworkConfigurationType();
        return NetworkConfigurationType.DHCP.equals(type)
                || NetworkConfigurationType.PPPOE.equals(type);
    }

    private boolean validateWan(final AssetConfiguration configuration) {
        if (isCarrierAssigned(configuration)) {
            // Clear anything left behind by a previous pass through this screen, so a switch from
            // static to DHCP does not ship a device configured with both.
            configuration.setWanIpAddress(null);
            configuration.setWanSubnetMask(null);
            configuration.setDefaultGateway(null);
            return true;
        }
        final List<String> messages = WAN_VALIDATOR.validate(configuration.getWanIpAddress(),
                configuration.getWanSubnetMask(), configuration.getDefaultGateway(),
                configuration.getPrimaryDnsAddress(), configuration.getSecondaryDnsAddress());
        return report(messages, "assetConfiguration.wanIpAddress");
    }

    private boolean validateLan(final AssetConfiguration configuration) {
        final LanValidator validator = lanValidatorFor(configuration.getAssetConfigurationType());
        if (validator == null) {
            addFieldErrorAndLog("assetConfiguration.assetConfigurationType",
                    "Choose a LAN configuration type.");
            return false;
        }
        // The LAN gateway is the customer's own router inside the site, not the device being
        // ordered - the two are different boxes and the validators reject them being the same
        // address. Type A additionally expects it at the last usable address in the subnet.
        final List<String> messages = validator.validate(configuration.getLanIpAddress(),
                configuration.getLanSubnetMask(), configuration.getLanGateway());
        return report(messages, "assetConfiguration.lanIpAddress");
    }

    private boolean validateBandwidth(final AssetConfiguration configuration) {
        final Integer bandwidth = configuration.getBandwidthKbps();
        if (bandwidth == null) {
            return true;
        }
        if (bandwidth.intValue() <= 0) {
            addFieldErrorAndLog("assetConfiguration.bandwidthKbps",
                    "Bandwidth must be greater than zero.");
            return false;
        }
        return true;
    }

    /**
     * Only the LAN types have a validator. WAN, dual WAN and HA pair describe how the device is
     * connected rather than how its LAN is numbered, and the LAN rules do not apply to them.
     */
    private static LanValidator lanValidatorFor(final AssetConfigurationType type) {
        if (AssetConfigurationType.LAN_TYPE_A.equals(type)) {
            return LAN_TYPE_A;
        }
        if (AssetConfigurationType.LAN_TYPE_B.equals(type)) {
            return LAN_TYPE_B;
        }
        if (AssetConfigurationType.LAN_TYPE_C.equals(type)) {
            return LAN_TYPE_C;
        }
        return null;
    }

    /**
     * The validators return every problem they found rather than the first, so all of them are
     * shown at once. They are attached to one field because the messages already name the field
     * they are about.
     */
    private boolean report(final List<String> messages, final String field) {
        if (messages == null || messages.isEmpty()) {
            return true;
        }
        for (final String message : messages) {
            addFieldErrorAndLog(field, message);
        }
        return false;
    }
}
