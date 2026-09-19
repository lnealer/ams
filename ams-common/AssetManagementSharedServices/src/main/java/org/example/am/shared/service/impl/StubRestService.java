package org.example.am.shared.service.impl;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.am.shared.domain.Address;
import org.example.am.shared.model.address.AddressValidationResponse;
import org.example.am.shared.model.address.ValidatedAddress;
import org.example.am.shared.model.address.ValidatedQualityType;
import org.example.am.shared.service.RestService;

/**
 * A canned address validation service, for environments that have no real one to call.
 *
 * <p>{@link RestServiceImpl} is the real implementation and stays exactly as it is; this is chosen
 * instead of it by profile, the same way the WebSEAL pre-authentication filter is. Pointing the
 * real client at a URL that does not answer would make every address check a socket timeout, which
 * is a slow and confusing way to discover that an environment simply has no validation service.</p>
 *
 * <p>The rules below are deliberately simple but not trivial: they exercise all three paths the
 * ordering flow has to handle - accepted as keyed, a correction to confirm, and the service being
 * unavailable - so the screens either side of them can be tested without a network.</p>
 */
public class StubRestService implements RestService {

    private static final Logger LOGGER = LogManager.getLogger(StubRestService.class);

    /** A ZIP ending in this is treated as unknown to the service, so the outage path is reachable. */
    private static final String UNAVAILABLE_ZIP_SUFFIX = "0000";

    @Override
    public AddressValidationResponse postAddressValidation(final Address address,
            final String requestId) {
        final AddressValidationResponse response = new AddressValidationResponse();
        response.setRequestId(requestId);

        if (address == null) {
            return error(response, "INVALID_REQUEST", "No address was supplied");
        }
        if (address.getZipCode() != null
                && address.getZipCode().trim().endsWith(UNAVAILABLE_ZIP_SUFFIX)) {
            LOGGER.info("Stub address validation: reporting the service as unavailable for ZIP {}",
                    address.getZipCode());
            return error(response, "SERVICE_UNAVAILABLE",
                    "The address validation service is not available in this environment");
        }

        final ValidatedAddress validated = new ValidatedAddress();
        validated.setId(requestId);
        validated.setAddressLine1(normalise(address.getAddressLine1()));
        validated.setAddressLine2(normalise(address.getAddressLine2()));
        validated.setCity(toTitleCase(address.getCity()));
        validated.setStateCode(address.getState() == null ? null : address.getState().getCode());
        validated.setPostalCode(address.getZipCode() == null ? null : address.getZipCode().trim());
        validated.setCountryCode(address.getCountry() == null ? "US" : address.getCountry().getCode());

        // CORRECTED only when the normalisation actually changed something. Returning it every
        // time would put a suggestion screen in front of every single order, which trains people
        // to click through it without reading.
        final boolean changed = differs(address.getAddressLine1(), validated.getAddressLine1())
                || differs(address.getAddressLine2(), validated.getAddressLine2())
                || differs(address.getCity(), validated.getCity());
        validated.setQuality(changed ? ValidatedQualityType.CORRECTED : ValidatedQualityType.EXACT);

        response.getAddresses().add(validated);
        return response;
    }

    /**
     * The tidying a real validation service does: collapse runs of whitespace, trim, and expand
     * the common street-type abbreviations to their postal forms.
     */
    private static String normalise(final String line) {
        if (line == null) {
            return null;
        }
        String result = line.trim().replaceAll("\\s+", " ");
        result = expand(result, "\\bSt\\.?\\b", "Street");
        result = expand(result, "\\bRd\\.?\\b", "Road");
        result = expand(result, "\\bAve\\.?\\b", "Avenue");
        result = expand(result, "\\bBlvd\\.?\\b", "Boulevard");
        result = expand(result, "\\bDr\\.?\\b", "Drive");
        result = expand(result, "\\bLn\\.?\\b", "Lane");
        result = expand(result, "\\bPkwy\\.?\\b", "Parkway");
        result = expand(result, "\\bSte\\.?\\b", "Suite");
        return result;
    }

    private static String expand(final String value, final String pattern, final String replacement) {
        return value.replaceAll("(?i)" + pattern, replacement);
    }

    private static String toTitleCase(final String city) {
        if (city == null) {
            return null;
        }
        final String trimmed = city.trim().replaceAll("\\s+", " ");
        final StringBuilder result = new StringBuilder(trimmed.length());
        boolean startOfWord = true;
        for (int i = 0; i < trimmed.length(); i++) {
            final char c = trimmed.charAt(i);
            if (startOfWord) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
            startOfWord = c == ' ' || c == '-' || c == '\'';
        }
        return result.toString();
    }

    private static boolean differs(final String keyed, final String validated) {
        if (keyed == null || validated == null) {
            return keyed != validated;
        }
        return !keyed.trim().toUpperCase(Locale.ENGLISH)
                .equals(validated.trim().toUpperCase(Locale.ENGLISH));
    }

    private static AddressValidationResponse error(final AddressValidationResponse response,
            final String code, final String message) {
        final org.example.am.shared.model.address.Error error =
                new org.example.am.shared.model.address.Error();
        error.setCode(code);
        error.setMessage(message);
        response.setError(error);
        return response;
    }
}
