package org.example.am.shared.dao;

import java.util.List;
import org.example.am.shared.domain.ETLStatus;

/**
 * Backs the administration utilities screen: the queue counters operations staff watch and the
 * ETL run status they check after the nightly load.
 */
public interface AdministrationDAO {

    int getQueueDepth(String queueName);

    ETLStatus getLastEtlStatus(String jobName);

    java.util.Date getLastEtlRunDate(String jobName);

    List<String> getStuckQueueEntries(String queueName, int olderThanMinutes);
}
