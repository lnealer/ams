package org.example.am.shared.domain;

import java.io.Serializable;

/**
 * Base class for the AMS "loadable type" hierarchy: a hand-rolled typesafe enum that predates
 * {@code java.lang.Enum} in this codebase and is retained because the constants carry a database
 * surrogate key alongside the business code.
 *
 * <p>Concrete subclasses expose their instances as {@code public static final} fields and register
 * them, in declaration order, into a {@link java.util.LinkedHashMap} keyed by code. Equality is by
 * concrete type plus code, so a deserialised instance still compares equal to the singleton.</p>
 */
public abstract class LoadableType implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;
    private String description;
    private Long databaseId;

    protected LoadableType() {
        super();
    }

    protected LoadableType(final String code, final String description, final Long databaseId) {
        this.code = code;
        this.description = description;
        this.databaseId = databaseId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(final Long databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }
        final LoadableType that = (LoadableType) other;
        return code == null ? that.code == null : code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return code == null ? 0 : code.hashCode();
    }

    /**
     * Renders the code, which is what the JSP/OGNL layer writes into hidden form fields.
     */
    @Override
    public String toString() {
        return code;
    }
}
