package org.example.am.shared.model.address;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The response body returned by the address validation service. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressValidationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;
    private List<ValidatedAddress> addresses = new ArrayList<ValidatedAddress>();
    private Error error;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }

    public List<ValidatedAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(final List<ValidatedAddress> addresses) {
        this.addresses = addresses == null ? new ArrayList<ValidatedAddress>() : addresses;
    }

    public Error getError() {
        return error;
    }

    public void setError(final Error error) {
        this.error = error;
    }

    public boolean isSuccessful() {
        return error == null;
    }

    /**
     * @return the first candidate, which is the best match; {@code null} when nothing came back
     */
    public ValidatedAddress getBestMatch() {
        return addresses.isEmpty() ? null : addresses.get(0);
    }

    /**
     * @return {@code true} when the caller should show the user a suggestion rather than accepting
     *         the address as keyed
     */
    public boolean hasSuggestion() {
        final ValidatedAddress best = getBestMatch();
        return best != null && best.isSuggestion();
    }
}
