package org.example.am.shared.dao;

import org.example.am.shared.domain.Address;

/** Reads and writes {@code AMS_ADDRESSES}. */
public interface AddressDAO {

    Address getAddress(long addressId);

    /** @return the generated address id */
    long insertAddress(Address address, String userId);

    int updateAddress(Address address, String userId);

    int markValidated(long addressId, boolean validated, String userId);
}
