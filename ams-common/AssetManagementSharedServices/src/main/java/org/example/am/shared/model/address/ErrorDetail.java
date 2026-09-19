package org.example.am.shared.model.address;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One field-level problem reported by the address validation service. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;
    private String issue;
    private String value;

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(final String issue) {
        this.issue = issue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return field + ": " + issue;
    }
}
