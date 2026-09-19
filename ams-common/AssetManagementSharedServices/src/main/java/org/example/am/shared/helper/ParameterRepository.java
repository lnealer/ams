package org.example.am.shared.helper;

import java.util.Date;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * A fluent builder over {@link MapSqlParameterSource}.
 *
 * <p>The DAOs build a lot of parameter maps and the plain {@code addValue} chain is easy to get
 * wrong when a value is conditional, so this adds the null-skipping and flag conversion cases in
 * one place.</p>
 */
public final class ParameterRepository {

    private final MapSqlParameterSource parameters = new MapSqlParameterSource();

    private ParameterRepository() {
        super();
    }

    public static ParameterRepository create() {
        return new ParameterRepository();
    }

    public static ParameterRepository of(final String name, final Object value) {
        return create().with(name, value);
    }

    public ParameterRepository with(final String name, final Object value) {
        parameters.addValue(name, value);
        return this;
    }

    /**
     * Adds the parameter only when the value is present, for SQL built with an optional predicate.
     */
    public ParameterRepository withOptional(final String name, final Object value) {
        if (value != null) {
            parameters.addValue(name, value);
        }
        return this;
    }

    public ParameterRepository withFlag(final String name, final boolean value) {
        parameters.addValue(name, value ? "Y" : "N");
        return this;
    }

    public ParameterRepository withDate(final String name, final Date value) {
        parameters.addValue(name, value == null ? null : new java.sql.Timestamp(value.getTime()));
        return this;
    }

    /**
     * Wraps the term for a case-insensitive {@code LIKE}, escaping the wildcards a user may type.
     */
    public ParameterRepository withLikeTerm(final String name, final String term) {
        if (term == null) {
            parameters.addValue(name, null);
            return this;
        }
        final String escaped = term.trim().toUpperCase()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        parameters.addValue(name, "%" + escaped + "%");
        return this;
    }

    public MapSqlParameterSource build() {
        return parameters;
    }
}
