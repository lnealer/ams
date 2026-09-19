package org.example.am.shared.model.address;

/**
 * How closely the validation service could match the submitted address.
 *
 * <p>A real Java enum, unlike the domain layer's loadable types: these values come from an external
 * contract and are never persisted, so there is no database id to carry.</p>
 */
public enum ValidatedQualityType {

    /** Matched exactly; the address may be used as keyed. */
    EXACT,

    /** Matched, but the service corrected or completed part of it - show the user the difference. */
    CORRECTED,

    /** Several candidates matched; the user has to pick one. */
    AMBIGUOUS,

    /** No match at all. */
    UNVERIFIED;

    public static ValidatedQualityType fromCode(final String code) {
        if (code == null) {
            return UNVERIFIED;
        }
        for (final ValidatedQualityType quality : values()) {
            if (quality.name().equalsIgnoreCase(code.trim())) {
                return quality;
            }
        }
        return UNVERIFIED;
    }

    public boolean isUsableWithoutPrompting() {
        return this == EXACT;
    }
}
