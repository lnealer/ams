package org.example.am.shared.dao;

import java.util.List;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.EmailQueueStatusType;
import org.example.am.shared.domain.EmailTemplateType;

/**
 * Reads and writes the outbound email queue. Mail is never sent inline: a row is queued inside the
 * business transaction and a separate poller delivers it, so a mail server outage cannot roll back an
 * order.
 */
public interface EmailDetailDAO {

    long queueEmail(EmailTemplateType template, EmailEntityType entityType, long entityId,
                String toAddress, String userId);

    List<Long> getQueuedEmailIds(int maxRows);

    int updateStatus(long emailId, EmailQueueStatusType status, String failureReason);
}
