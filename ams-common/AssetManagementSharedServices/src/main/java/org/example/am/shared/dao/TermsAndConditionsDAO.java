package org.example.am.shared.dao;

/**
 * Tracks which version of the terms and conditions each user has accepted.
 */
public interface TermsAndConditionsDAO {

    String getCurrentVersion();

    boolean hasAccepted(String userId, String version);

    int recordAcceptance(String userId, String version);
}
