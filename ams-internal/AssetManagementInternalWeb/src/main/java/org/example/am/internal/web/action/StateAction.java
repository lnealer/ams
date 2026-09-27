package org.example.am.internal.web.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.struts2.Action;
import org.example.am.shared.domain.CountryType;
import org.example.am.shared.domain.StateType;
import org.example.am.shared.domain.comparator.CountryComparator;
import org.example.am.shared.domain.comparator.StateComparator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Serves the state and country drop-downs.
 */
@Component("StateAction")
@Scope("prototype")
public class StateAction extends BaseAction {

    private static final long serialVersionUID = 1L;

    private List<StateType> states;
    private List<CountryType> countries;


    public String listStates() throws Exception {
        states = new ArrayList<StateType>(StateType.values());
        Collections.sort(states, new StateComparator());
        countries = new ArrayList<CountryType>(CountryType.values());
        Collections.sort(countries, new CountryComparator());
        return Action.SUCCESS;
    }

    public List<StateType> getStates() {
        return states;
    }

    public void setStates(final List<StateType> states) {
        this.states = states;
    }

    public List<CountryType> getCountries() {
        return countries;
    }

    public void setCountries(final List<CountryType> countries) {
        this.countries = countries;
    }
}
