package org.example.am.shared.domain;

import java.io.Serializable;

/**
 * A postal address used for shipping hardware and for scheduling the installation visit.
 *
 * <p>{@link #toString()} renders the formatted mailing address that the order confirmation screen,
 * the shipping label feed and the confirmation email all reuse, so the formatting rules live here
 * rather than being repeated in three JSPs.</p>
 */
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Carrier label line limits enforced by {@code OrderBaseAction}. */
    public static final int MAX_ADDRESS_LINE_LENGTH = 26;
    public static final int MAX_CITY_LENGTH = 20;

    private Long addressId;
    private AddressType addressType;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String zipCode;
    private String county;
    private StateType state;
    private CountryType country;
    private String attentionTo;
    private boolean validated;

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(final Long addressId) {
        this.addressId = addressId;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(final AddressType addressType) {
        this.addressType = addressType;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(final String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(final String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(final String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(final String county) {
        this.county = county;
    }

    public StateType getState() {
        return state;
    }

    public void setState(final StateType state) {
        this.state = state;
    }

    public CountryType getCountry() {
        return country;
    }

    public void setCountry(final CountryType country) {
        this.country = country;
    }

    public String getAttentionTo() {
        return attentionTo;
    }

    public void setAttentionTo(final String attentionTo) {
        this.attentionTo = attentionTo;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(final boolean validated) {
        this.validated = validated;
    }

    /**
     * Only domestic addresses go through the outbound address validation service; everything else
     * is accepted as keyed.
     */
    public boolean isDomestic() {
        return CountryType.US.equals(country);
    }

    /**
     * @return {@code true} when either address line or the city exceeds what the carrier label
     *         accepts. Mirrors the check {@code OrderBaseAction} runs before review.
     */
    public boolean isOverCarrierLabelLimits() {
        return exceeds(addressLine1, MAX_ADDRESS_LINE_LENGTH)
                || exceeds(addressLine2, MAX_ADDRESS_LINE_LENGTH)
                || exceeds(city, MAX_CITY_LENGTH);
    }

    private static boolean exceeds(final String value, final int limit) {
        return value != null && value.trim().length() > limit;
    }

    public boolean isComplete() {
        return notBlank(addressLine1) && notBlank(city) && notBlank(zipCode)
                && state != null && country != null;
    }

    private static boolean notBlank(final String value) {
        return value != null && value.trim().length() > 0;
    }

    /**
     * @return the address as it is printed on a label:
     *         <pre>
     *         ATTN: Jane Doe
     *         100 Main Street
     *         Suite 400
     *         Springfield, IL 62704
     *         United States
     *         </pre>
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        appendLine(builder, notBlank(attentionTo) ? "ATTN: " + attentionTo.trim() : null);
        appendLine(builder, addressLine1);
        appendLine(builder, addressLine2);

        final StringBuilder cityLine = new StringBuilder();
        if (notBlank(city)) {
            cityLine.append(city.trim());
        }
        if (state != null) {
            if (cityLine.length() > 0) {
                cityLine.append(", ");
            }
            cityLine.append(state.getCode());
        }
        if (notBlank(zipCode)) {
            if (cityLine.length() > 0) {
                cityLine.append(' ');
            }
            cityLine.append(zipCode.trim());
        }
        appendLine(builder, cityLine.length() == 0 ? null : cityLine.toString());

        if (country != null && !CountryType.US.equals(country)) {
            appendLine(builder, country.getDescription());
        }
        return builder.toString();
    }

    private static void appendLine(final StringBuilder builder, final String line) {
        if (line == null || line.trim().length() == 0) {
            return;
        }
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(line.trim());
    }
}
