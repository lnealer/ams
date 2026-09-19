package org.example.am.internal.web.action;

import java.util.List;

import com.opensymphony.xwork2.Action;
import org.example.am.internal.service.SaveForLaterService;
import org.example.am.shared.domain.SaveForLaterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Lists everything the operator has parked, across all the flows.
 */
@Component("SavedFormAction")
@Scope("prototype")
public class SavedFormAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private List<Long> savedOrders;
    private List<Long> savedRequests;

    @Autowired
    private transient SaveForLaterService internalSaveForLaterService;

    public String listSavedForms() throws Exception {
        savedOrders = internalSaveForLaterService.getSavedCustomerIds(getUserId(),
                SaveForLaterType.ORDER);
        savedRequests = internalSaveForLaterService.getSavedCustomerIds(getUserId(),
                SaveForLaterType.NETWORK_CHANGE_REQUEST);
        return Action.SUCCESS;
    }

    public List<Long> getSavedOrders() {
        return savedOrders;
    }

    public void setSavedOrders(final List<Long> savedOrders) {
        this.savedOrders = savedOrders;
    }

    public List<Long> getSavedRequests() {
        return savedRequests;
    }

    public void setSavedRequests(final List<Long> savedRequests) {
        this.savedRequests = savedRequests;
    }
}
