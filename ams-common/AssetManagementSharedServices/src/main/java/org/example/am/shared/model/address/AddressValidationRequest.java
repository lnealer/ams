package org.example.am.shared.model.address;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/** The request body posted to the address validation service. */
@JsonInclude(Include.NON_NULL)
public class AddressValidationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private String sourceSystem;
    private List<ValidatableAddress> addresses = new ArrayList<ValidatableAddress>();

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(final String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public List<ValidatableAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(final List<ValidatableAddress> addresses) {
        this.addresses = addresses == null ? new ArrayList<ValidatableAddress>() : addresses;
    }

    public void addAddress(final ValidatableAddress address) {
        addresses.add(address);
    }
}
