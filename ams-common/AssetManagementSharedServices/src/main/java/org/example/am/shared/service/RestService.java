package org.example.am.shared.service;

import org.example.am.shared.domain.Address;
import org.example.am.shared.model.address.AddressValidationResponse;

/**
 * Outbound calls to the platform's shared REST services.
 */
public interface RestService {

    /**
     * Validates a shipping address.
     *
     * @return the service's response, or a response carrying an {@code error} when the call failed;
     *         never {@code null}, because a validation outage must not block an order
     */
    AddressValidationResponse postAddressValidation(Address address, String requestId);
}
