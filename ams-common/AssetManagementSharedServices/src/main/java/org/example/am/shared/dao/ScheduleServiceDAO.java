package org.example.am.shared.dao;

import java.util.Date;

/**
 * Reads and writes the scheduled dates held against orders, change requests and decommissions.
 */
public interface ScheduleServiceDAO {

    int updateInstallationDate(long orderId, java.util.Date scheduledDate, String userId);

    int updateNetworkChangeDate(long networkChangeRequestId, java.util.Date scheduledDate,
                String userId);

    int updateDecommissionDate(long decommissionId, java.util.Date scheduledDate, String userId);
}
