package org.example.am.shared.dao;

import java.util.List;
import org.example.am.shared.domain.QueueStatusType;

/**
 * The asynchronous work queue that carries configuration changes out to the devices.
 */
public interface ModifyQueueDAO {

    long enqueue(String queueName, String entityReference, String payload, String userId);

    List<Long> claimNext(String queueName, int maxRows);

    int updateStatus(long queueId, QueueStatusType status, String failureReason);
}
