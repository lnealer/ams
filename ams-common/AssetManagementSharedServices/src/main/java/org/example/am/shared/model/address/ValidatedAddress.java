package org.example.am.shared.model.address;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One validated address returned by the service, with the quality of the match.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidatedAddress implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private ValidatedQualityType quality;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateCode;
    private String postalCode;
    private String postalCodeExtension;
    private String countyName;
    private String countryCode;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public ValidatedQualityType getQuality() {
        return quality;
    }

    public void setQuality(final ValidatedQualityType quality) {
        this.quality = quality;
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

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(final String stateCode) {
        this.stateCode = stateCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostalCodeExtension() {
        return postalCodeExtension;
    }

    public void setPostalCodeExtension(final String postalCodeExtension) {
        this.postalCodeExtension = postalCodeExtension;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(final String countyName) {
        this.countyName = countyName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @return {@code true} when this candidate differs from what the user keyed and should be
     *         offered as a suggestion
     */
    public boolean isSuggestion() {
        return ValidatedQualityType.CORRECTED.equals(quality)
                || ValidatedQualityType.AMBIGUOUS.equals(quality);
    }
}
