package org.example.am.shared.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A business customer - typically a small or mid-sized company - that holds managed network
 * services and owns the hardware installed at its sites.
 *
 * <p>Deliberately not a {@link BaseDomain}: customer records are mastered upstream and AMS never
 * writes the audit columns for them. The collections are populated selectively - the search grid
 * gets a customer with nothing but identity, while the customer administration screen gets one
 * with assets, services and contacts filled in.</p>
 */
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private String customerName;
    private String customerShortName;
    private String accountNumber;

    private List<Asset> assets = new ArrayList<Asset>();
    private List<AmsService> services = new ArrayList<AmsService>();
    private List<Contact> contacts = new ArrayList<Contact>();
    private List<Event> events = new ArrayList<Event>();

    private TechLine techLine;
    private Address address;
    private CustomerEarlyAdopter customerEarlyAdopter;

    private boolean canSubmitOrders;
    private boolean active;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(final String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerShortName() {
        return customerShortName;
    }

    public void setCustomerShortName(final String customerShortName) {
        this.customerShortName = customerShortName;
    }

    /**
     * @return the billing account number this customer is known by. Numeric, which is what lets
     *         the single search box treat an all-digit term as an account lookup.
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(final List<Asset> assets) {
        this.assets = assets == null ? new ArrayList<Asset>() : assets;
    }

    public List<AmsService> getServices() {
        return services;
    }

    public void setServices(final List<AmsService> services) {
        this.services = services == null ? new ArrayList<AmsService>() : services;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(final List<Contact> contacts) {
        this.contacts = contacts == null ? new ArrayList<Contact>() : contacts;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(final List<Event> events) {
        this.events = events == null ? new ArrayList<Event>() : events;
    }

    public TechLine getTechLine() {
        return techLine;
    }

    public void setTechLine(final TechLine techLine) {
        this.techLine = techLine;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public CustomerEarlyAdopter getCustomerEarlyAdopter() {
        return customerEarlyAdopter;
    }

    public void setCustomerEarlyAdopter(final CustomerEarlyAdopter customerEarlyAdopter) {
        this.customerEarlyAdopter = customerEarlyAdopter;
    }

    public boolean isCanSubmitOrders() {
        return canSubmitOrders;
    }

    public void setCanSubmitOrders(final boolean canSubmitOrders) {
        this.canSubmitOrders = canSubmitOrders;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    // ------------------------------------------------------------------
    // Business rules
    // ------------------------------------------------------------------

    public boolean hasActiveService() {
        for (final AmsService service : services) {
            if (service.isActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasActiveService(final ServiceType serviceType) {
        for (final AmsService service : services) {
            if (service.isActive() && service.getServiceType() != null
                    && service.getServiceType().equals(serviceType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ordering requires an active customer, at least one active service, and the administration
     * flag that operations staff toggle from the customer administration screen.
     */
    public boolean isOrderingEnabled() {
        return active && canSubmitOrders && hasActiveService();
    }

    public boolean isEarlyAdopter() {
        return customerEarlyAdopter != null && customerEarlyAdopter.isEarlyAdopter();
    }

    public Contact getContactByType(final ContactType contactType) {
        for (final Contact contact : contacts) {
            if (contact.isActive() && contactType != null
                    && contactType.equals(contact.getContactType())) {
                return contact;
            }
        }
        return null;
    }

    /**
     * @return every asset the migration flow may offer for this customer.
     */
    public List<Asset> getMigratableAssets() {
        final List<Asset> migratable = new ArrayList<Asset>();
        for (final Asset asset : assets) {
            if (asset.getMigrationProblems().isEmpty()) {
                migratable.add(asset);
            }
        }
        return migratable;
    }

    /**
     * @return "Customer Name (12345678)" as shown in the search grid and the page header.
     */
    public String getDisplayName() {
        if (customerName == null) {
            return String.valueOf(customerId);
        }
        return customerName + (customerId == null ? "" : " (" + customerId + ")");
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
