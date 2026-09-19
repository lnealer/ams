package org.example.am.internal.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.example.am.shared.domain.Address;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.Contact;
import org.example.am.shared.domain.AssetConfiguration;
import org.example.am.shared.domain.AssetConfigurationType;
import org.example.am.shared.domain.CountryType;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.domain.DayType;
import org.example.am.shared.domain.HourType;
import org.example.am.shared.domain.NetworkConfigurationType;
import org.example.am.shared.domain.StateType;
import org.example.am.shared.domain.MaintenanceWindow;
import org.example.am.shared.domain.Order;
import org.example.am.shared.domain.OrderType;
import org.example.am.shared.domain.ShippingCarrier;
import org.example.am.shared.domain.SubscriberPc;
import org.example.am.shared.domain.Timeslot;

/**
 * The form backing the multi-step ordering flow.
 *
 * <p>Ordering spans several screens, so the partly completed order is carried in the session as
 * this model rather than being persisted at each step: an abandoned order should leave nothing
 * behind. {@link #toOrder()} turns it into the domain object only at submit time.</p>
 */
public class OrderModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private Customer customer;
    private Asset selectedAsset;
    private Asset assetBeingReplaced;

    private OrderType orderType;
    private ShippingCarrier shippingCarrier;

    private Address shippingAddress = new Address();
    private Contact orderingContact;
    private Contact shippingContact;
    private Contact installationContact;

    private Date requestedInstallationDate;
    private Long installationTimeslotId;
    private Long techlineTimeslotId;
    private String comments;

    private boolean dueDiligenceCompleted;
    private String dueDiligenceWaiverReason;

    /** Set by the address validation interceptor when it has a better address to offer. */
    private Address suggestedAddress;
    private boolean addressSuggestionAccepted;

    /** The customer's own name for the device, e.g. "Front desk router". */
    private String deviceNickname;

    /**
     * Whether suggested values have already been offered for the configuration and the subscriber
     * grid. Suggestions are made once, on first arrival: re-applying them every time the operator
     * steps back through the flow would silently refill a field they had deliberately emptied.
     */
    private boolean configurationDefaultsApplied;
    private boolean subscriberDefaultsApplied;

    private MaintenanceWindow maintenanceWindow = new MaintenanceWindow();
    private AssetConfiguration assetConfiguration = new AssetConfiguration();

    /**
     * The subscriber machine grid.
     *
     * <p>Pre-sized with blank rows by the capture step, because Struts binds
     * {@code subscriberPcs[3].hostName} onto whatever is already at that index. The blank ones are
     * filtered out on the way to the database.</p>
     */
    private List<SubscriberPc> subscriberPcs = new ArrayList<SubscriberPc>();

    private Long shippingWindowTimeslotId;
    private Timeslot shippingWindow;

    /** How far the user has got, so the step indicator can show it and a skipped step be caught. */
    private int furthestStepReached = 1;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    public Asset getSelectedAsset() {
        return selectedAsset;
    }

    public void setSelectedAsset(final Asset selectedAsset) {
        this.selectedAsset = selectedAsset;
    }

    public Asset getAssetBeingReplaced() {
        return assetBeingReplaced;
    }

    public void setAssetBeingReplaced(final Asset assetBeingReplaced) {
        this.assetBeingReplaced = assetBeingReplaced;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(final OrderType orderType) {
        this.orderType = orderType;
    }

    public ShippingCarrier getShippingCarrier() {
        return shippingCarrier;
    }

    public void setShippingCarrier(final ShippingCarrier shippingCarrier) {
        this.shippingCarrier = shippingCarrier;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(final Address shippingAddress) {
        this.shippingAddress = shippingAddress == null ? new Address() : shippingAddress;
    }

    public Contact getOrderingContact() {
        return orderingContact;
    }

    public void setOrderingContact(final Contact orderingContact) {
        this.orderingContact = orderingContact;
    }

    public Contact getShippingContact() {
        return shippingContact;
    }

    public void setShippingContact(final Contact shippingContact) {
        this.shippingContact = shippingContact;
    }

    public Contact getInstallationContact() {
        return installationContact;
    }

    public void setInstallationContact(final Contact installationContact) {
        this.installationContact = installationContact;
    }

    public Date getRequestedInstallationDate() {
        return requestedInstallationDate;
    }

    public void setRequestedInstallationDate(final Date requestedInstallationDate) {
        this.requestedInstallationDate = requestedInstallationDate;
    }

    public Long getInstallationTimeslotId() {
        return installationTimeslotId;
    }

    public void setInstallationTimeslotId(final Long installationTimeslotId) {
        this.installationTimeslotId = installationTimeslotId;
    }

    public Long getTechlineTimeslotId() {
        return techlineTimeslotId;
    }

    public void setTechlineTimeslotId(final Long techlineTimeslotId) {
        this.techlineTimeslotId = techlineTimeslotId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public boolean isDueDiligenceCompleted() {
        return dueDiligenceCompleted;
    }

    public void setDueDiligenceCompleted(final boolean dueDiligenceCompleted) {
        this.dueDiligenceCompleted = dueDiligenceCompleted;
    }

    public String getDueDiligenceWaiverReason() {
        return dueDiligenceWaiverReason;
    }

    public void setDueDiligenceWaiverReason(final String dueDiligenceWaiverReason) {
        this.dueDiligenceWaiverReason = dueDiligenceWaiverReason;
    }

    public Address getSuggestedAddress() {
        return suggestedAddress;
    }

    public void setSuggestedAddress(final Address suggestedAddress) {
        this.suggestedAddress = suggestedAddress;
    }

    public boolean isAddressSuggestionAccepted() {
        return addressSuggestionAccepted;
    }

    public void setAddressSuggestionAccepted(final boolean addressSuggestionAccepted) {
        this.addressSuggestionAccepted = addressSuggestionAccepted;
    }

    public String getDeviceNickname() {
        return deviceNickname;
    }

    public void setDeviceNickname(final String deviceNickname) {
        this.deviceNickname = deviceNickname;
    }

    public MaintenanceWindow getMaintenanceWindow() {
        return maintenanceWindow;
    }

    public void setMaintenanceWindow(final MaintenanceWindow maintenanceWindow) {
        this.maintenanceWindow = maintenanceWindow == null ? new MaintenanceWindow() : maintenanceWindow;
    }

    public AssetConfiguration getAssetConfiguration() {
        return assetConfiguration;
    }

    public void setAssetConfiguration(final AssetConfiguration assetConfiguration) {
        this.assetConfiguration = assetConfiguration == null
                ? new AssetConfiguration() : assetConfiguration;
    }

    public List<SubscriberPc> getSubscriberPcs() {
        return subscriberPcs;
    }

    public void setSubscriberPcs(final List<SubscriberPc> subscriberPcs) {
        this.subscriberPcs = subscriberPcs == null ? new ArrayList<SubscriberPc>() : subscriberPcs;
    }

    /**
     * Grows the grid to {@code rows} entries so Struts has something to bind each index onto.
     *
     * <p>Only ever adds. Shrinking it would silently discard a machine the user had already keyed
     * if they went back a step.</p>
     */
    public void ensureSubscriberPcRows(final int rows) {
        while (subscriberPcs.size() < rows) {
            subscriberPcs.add(new SubscriberPc());
        }
    }

    /** @return only the rows the user actually filled in */
    public List<SubscriberPc> getPopulatedSubscriberPcs() {
        final List<SubscriberPc> populated = new ArrayList<SubscriberPc>();
        for (final SubscriberPc pc : subscriberPcs) {
            if (pc != null && !pc.isBlank()) {
                populated.add(pc);
            }
        }
        return populated;
    }

    public Long getShippingWindowTimeslotId() {
        return shippingWindowTimeslotId;
    }

    public void setShippingWindowTimeslotId(final Long shippingWindowTimeslotId) {
        this.shippingWindowTimeslotId = shippingWindowTimeslotId;
    }

    public Timeslot getShippingWindow() {
        return shippingWindow;
    }

    public void setShippingWindow(final Timeslot shippingWindow) {
        this.shippingWindow = shippingWindow;
        if (shippingWindow != null) {
            this.shippingWindowTimeslotId = shippingWindow.getTimeslotId();
        }
    }

    public int getFurthestStepReached() {
        return furthestStepReached;
    }

    public void setFurthestStepReached(final int furthestStepReached) {
        this.furthestStepReached = furthestStepReached;
    }

    /** Records that a step has been completed, without ever moving the marker backwards. */
    public boolean isConfigurationDefaultsApplied() {
        return configurationDefaultsApplied;
    }

    public void setConfigurationDefaultsApplied(final boolean configurationDefaultsApplied) {
        this.configurationDefaultsApplied = configurationDefaultsApplied;
    }

    public boolean isSubscriberDefaultsApplied() {
        return subscriberDefaultsApplied;
    }

    public void setSubscriberDefaultsApplied(final boolean subscriberDefaultsApplied) {
        this.subscriberDefaultsApplied = subscriberDefaultsApplied;
    }

    public void reachStep(final int step) {
        if (step > furthestStepReached) {
            furthestStepReached = step;
        }
    }

    /*
     * Code accessors for the drop-downs.
     *
     * Every one of these types is a legacy typesafe enum with a private constructor, so a form
     * cannot bind to a nested "state.code" or "dayType.code": OGNL would have to construct one and
     * cannot, and the failure is silent - the field simply stays null and the user's choice
     * vanishes. Binding to a plain code and looking the instance up here is how the rest of the
     * application does it, and it keeps the lookup in one place rather than in each action.
     */

    public String getStateCode() {
        return shippingAddress == null || shippingAddress.getState() == null
                ? null : shippingAddress.getState().getCode();
    }

    public void setStateCode(final String stateCode) {
        if (shippingAddress == null) {
            shippingAddress = new Address();
        }
        shippingAddress.setState(StateType.lookup(stateCode));
    }

    public String getCountryCode() {
        return shippingAddress == null || shippingAddress.getCountry() == null
                ? null : shippingAddress.getCountry().getCode();
    }

    public void setCountryCode(final String countryCode) {
        if (shippingAddress == null) {
            shippingAddress = new Address();
        }
        shippingAddress.setCountry(CountryType.lookup(countryCode));
    }

    public String getMaintenanceDayCode() {
        return maintenanceWindow.getDayType() == null
                ? null : maintenanceWindow.getDayType().getCode();
    }

    public void setMaintenanceDayCode(final String maintenanceDayCode) {
        maintenanceWindow.setDayType(DayType.lookup(maintenanceDayCode));
    }

    public String getMaintenanceStartHourCode() {
        return maintenanceWindow.getStartHour() == null
                ? null : maintenanceWindow.getStartHour().getCode();
    }

    public void setMaintenanceStartHourCode(final String maintenanceStartHourCode) {
        maintenanceWindow.setStartHour(HourType.lookup(maintenanceStartHourCode));
    }

    public String getMaintenanceEndHourCode() {
        return maintenanceWindow.getEndHour() == null
                ? null : maintenanceWindow.getEndHour().getCode();
    }

    public void setMaintenanceEndHourCode(final String maintenanceEndHourCode) {
        maintenanceWindow.setEndHour(HourType.lookup(maintenanceEndHourCode));
    }

    public String getNetworkConfigurationCode() {
        return assetConfiguration.getNetworkConfigurationType() == null
                ? null : assetConfiguration.getNetworkConfigurationType().getCode();
    }

    public void setNetworkConfigurationCode(final String networkConfigurationCode) {
        assetConfiguration.setNetworkConfigurationType(
                NetworkConfigurationType.lookup(networkConfigurationCode));
    }

    public String getConfigurationTypeCode() {
        return assetConfiguration.getAssetConfigurationType() == null
                ? null : assetConfiguration.getAssetConfigurationType().getCode();
    }

    public void setConfigurationTypeCode(final String configurationTypeCode) {
        assetConfiguration.setAssetConfigurationType(
                AssetConfigurationType.lookup(configurationTypeCode));
    }

    /** @return {@code true} when this flow replaces existing hardware rather than adding to it */
    public boolean isReplacement() {
        return OrderType.REPLACEMENT.equals(orderType)
                || OrderType.EMERGENCY_REPLACEMENT.equals(orderType)
                || OrderType.MIGRATION.equals(orderType);
    }

    /**
     * Builds the domain object the service layer persists.
     *
     * <p>Only called at submit time: everything before that is form state.</p>
     */
    public Order toOrder() {
        final Order order = new Order();
        order.setCustomerId(customerId);
        order.setOrderType(orderType);
        order.setAsset(selectedAsset);
        order.setAssetBeingReplaced(assetBeingReplaced);
        order.setShippingAddress(shippingAddress);
        order.setOrderingContact(orderingContact);
        order.setShippingContact(shippingContact);
        order.setInstallationContact(installationContact);
        order.setShippingCarrier(shippingCarrier);
        order.setRequestedInstallationDate(requestedInstallationDate);
        order.setComments(comments);
        order.setDeviceNickname(deviceNickname);
        order.setMaintenanceWindow(maintenanceWindow);
        order.setAssetConfiguration(assetConfiguration);
        order.setSubscriberPcs(getPopulatedSubscriberPcs());
        order.setShippingWindow(shippingWindow);
        order.setShippingWindowTimeslotId(shippingWindowTimeslotId);

        final org.example.am.shared.domain.DueDiligence dueDiligence =
                new org.example.am.shared.domain.DueDiligence();
        dueDiligence.setCompleted(dueDiligenceCompleted);
        if (!dueDiligenceCompleted && dueDiligenceWaiverReason != null
                && dueDiligenceWaiverReason.trim().length() > 0) {
            dueDiligence.setWaived(true);
            dueDiligence.setWaiverReason(dueDiligenceWaiverReason.trim());
        }
        order.setDueDiligence(dueDiligence);
        return order;
    }
}
