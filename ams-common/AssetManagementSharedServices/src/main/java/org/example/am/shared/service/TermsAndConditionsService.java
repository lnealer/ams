package org.example.am.shared.service;

/**
 * Gates the application on acceptance of the current terms and conditions.
 */
public interface TermsAndConditionsService {

    String getCurrentVersion();

    /**
     * @return {@code true} when the user has not accepted the version now in force
     */
    boolean isAcceptanceRequired(String userId);

    void accept(String userId);
}
