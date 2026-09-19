package org.example.am.shared.service;

import java.util.List;
import java.util.Map;
import org.example.am.shared.domain.ETLStatus;

/**
 * Backs the administration utilities screen.
 */
public interface AdministrationService {

    Map<String, Integer> getQueueDepths(List<String> queueNames);

    ETLStatus getLastEtlStatus(String jobName);

    List<String> getStuckEntries(String queueName, int olderThanMinutes);

    Map<String, String> getAllProperties();
}
