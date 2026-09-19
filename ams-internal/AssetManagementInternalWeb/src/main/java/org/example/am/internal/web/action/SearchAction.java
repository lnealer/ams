package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.List;

import org.example.am.internal.security.SecurityRoleType;
import org.example.am.internal.service.CustomerService;
import org.example.am.internal.web.model.AssetGridRow;
import org.example.am.internal.web.model.SearchModel;
import org.example.am.shared.domain.Asset;
import org.example.am.shared.domain.Customer;
import org.example.am.shared.service.AssetSearchService;
import org.example.am.shared.service.CustomerSearchService;
import org.example.am.shared.utils.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.Action;

/**
 * The asset search screen.
 *
 * <p>The entry point to almost everything an operator does: they arrive knowing a serial number or
 * a customer name and leave with an asset to act on.</p>
 */
@Component("SearchAction")
@Scope("prototype")
public class SearchAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    /** Below this many characters a search matches so much that the result is useless. */
    private static final int MINIMUM_TERM_LENGTH = 2;

    private final SearchModel model = new SearchModel();

    private List<Customer> customers = new ArrayList<Customer>();
    private String customerTerm;
    private Long selectedCustomerId;

    @Autowired
    private transient AssetSearchService assetSearchService;

    @Autowired
    private transient CustomerSearchService customerSearchService;

    @Autowired
    private transient CustomerService internalCustomerService;

    @Autowired
    private transient org.example.am.shared.service.CustomerService customerService;

    @Override
    public SearchModel getModel() {
        return model;
    }

    /** Renders the empty search form. */
    public String initSearch() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SEARCH_ASSETS);
        if (denied != null) {
            return denied;
        }
        model.setMaxRows(CommonConstants.MAX_SEARCH_RESULTS);
        return Action.SUCCESS;
    }

    /**
     * Runs the search.
     *
     * @return {@code input} when the term is unusable, so the user stays on the form with the
     *         reason, rather than being shown an empty grid they cannot interpret
     */
    public String search() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SEARCH_ASSETS);
        if (denied != null) {
            return denied;
        }
        if (!validateUserSearch()) {
            return Action.INPUT;
        }

        final int maxRows = model.getMaxRows() <= 0
                ? CommonConstants.MAX_SEARCH_RESULTS : model.getMaxRows();
        final List<Asset> assets = assetSearchService.search(model.getSearchCriteriaType(),
                model.getSearchTerm(), maxRows);

        final List<AssetGridRow> rows = new ArrayList<AssetGridRow>(assets.size());
        for (final Asset asset : assets) {
            rows.add(AssetGridRow.from(asset));
        }
        model.setResults(rows);

        final int total = assetSearchService.countMatches(model.getSearchCriteriaType(),
                model.getSearchTerm());
        model.setTotalMatches(total);
        model.setTruncated(total > rows.size());

        if (model.isTruncated()) {
            addActionMessage("Showing the first " + rows.size() + " of " + total
                    + " matches. Narrow the search to see the rest.");
        }
        logger.info("User {} searched {} for a term of {} characters and matched {} assets",
                getUserId(), model.getSearchCriteria(),
                Integer.valueOf(model.getSearchTerm().trim().length()), Integer.valueOf(total));
        return Action.SUCCESS;
    }

    /**
     * The customer picker's type-ahead.
     *
     * @return a JSON list of matching customers
     */
    public String getCustomers() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SEARCH_CUSTOMERS);
        if (denied != null) {
            return denied;
        }
        if (customerTerm == null || customerTerm.trim().length() < MINIMUM_TERM_LENGTH) {
            customers = new ArrayList<Customer>();
            return Action.SUCCESS;
        }
        customers = customerSearchService.search(customerTerm.trim());
        return Action.SUCCESS;
    }

    /**
     * Renders the customer picker.
     *
     * <p>Lists every customer rather than requiring a search term: on first use the operator has
     * not chosen anyone yet and has nothing to type. Most screens refuse to do anything until a
     * customer is selected, so this is the entry point to the whole application.</p>
     */
    public String initCustomerPicker() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_SEARCH_CUSTOMERS);
        if (denied != null) {
            return denied;
        }
        customers = customerTerm == null || customerTerm.trim().length() == 0
                ? customerSearchService.listAll(CommonConstants.MAX_SEARCH_RESULTS)
                : customerSearchService.search(customerTerm.trim());
        return Action.SUCCESS;
    }

    /**
     * Selects the customer the operator will act on behalf of for the rest of the session.
     *
     * <p>Looked up by id through {@code CustomerService}, not through the search service: search
     * matches on name or account number, so feeding it a customer id found nothing and the
     * selection silently failed.</p>
     */
    public String selectCustomer() throws Exception {
        final String denied = requireRole(SecurityRoleType.INT_VIEW_CUSTOMER);
        if (denied != null) {
            return denied;
        }
        if (selectedCustomerId == null) {
            addActionError("Choose a customer first.");
            return initCustomerPicker();
        }
        final Customer customer = customerService.getCustomer(selectedCustomerId.longValue());
        if (customer == null) {
            addActionError("That customer could not be found.");
            return initCustomerPicker();
        }
        setCurrentCustomer(customer);
        internalCustomerService.recordVisit(getUserId(), selectedCustomerId.longValue());
        addActionMessage("You are now working on behalf of " + customer.getDisplayName() + ".");
        return Action.SUCCESS;
    }

    /** Clears the selection, so the operator cannot act on a customer by accident. */
    public String clearCustomer() throws Exception {
        setCurrentCustomer(null);
        addActionMessage("Customer selection cleared.");
        return initCustomerPicker();
    }

    /**
     * Checks the search is worth running.
     *
     * <p>Public because the search JSP also calls it to decide whether to enable the button, and
     * because it is the piece of this action most worth testing on its own.</p>
     *
     * @return {@code true} when the search may proceed
     */
    public boolean validateUserSearch() {
        if (model.getSearchCriteriaType() == null) {
            addFieldErrorAndLog("searchCriteria", "Choose what to search by.");
            return false;
        }
        final String term = model.getSearchTerm();
        if (term == null || term.trim().length() == 0) {
            addFieldErrorAndLog("searchTerm", "Enter something to search for.");
            return false;
        }
        if (term.trim().length() < MINIMUM_TERM_LENGTH) {
            addFieldErrorAndLog("searchTerm",
                    "Enter at least " + MINIMUM_TERM_LENGTH + " characters.");
            return false;
        }
        return true;
    }

    public List<Customer> getCustomerList() {
        return customers;
    }

    public String getCustomerTerm() {
        return customerTerm;
    }

    public void setCustomerTerm(final String customerTerm) {
        this.customerTerm = customerTerm;
    }

    public Long getSelectedCustomerId() {
        return selectedCustomerId;
    }

    public void setSelectedCustomerId(final Long selectedCustomerId) {
        this.selectedCustomerId = selectedCustomerId;
    }
}
