package org.example.am.shared.model.address;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The error envelope the address validation service returns.
 *
 * <p>Named to match the external contract's field, which is why it shadows {@code java.lang.Error};
 * it is confined to this package and is never thrown.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Error implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;
    private String message;
    private List<ErrorDetail> details = new ArrayList<ErrorDetail>();

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public List<ErrorDetail> getDetails() {
        return details;
    }

    public void setDetails(final List<ErrorDetail> details) {
        this.details = details == null ? new ArrayList<ErrorDetail>() : details;
    }

    @Override
    public String toString() {
        return code + " " + message + (details.isEmpty() ? "" : " " + details);
    }
}
