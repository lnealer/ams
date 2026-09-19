package org.example.am.shared.service;

import java.util.List;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.EmailQueueStatusType;
import org.example.am.shared.domain.EmailTemplateType;

/**
 * Queues templated notifications. Nothing here talks to a mail server: rows are written inside the
 * caller's transaction and delivered by a separate poller.
 */
public interface EmailService {

    /**
     * Resolves recipients from the entity's contacts and suppresses duplicates.
     *
     * @return the queued email id, or {@code null} when a duplicate was suppressed
     */
    Long queueEntityEmail(EmailTemplateType template, EmailEntityType entityType, long entityId,
                String userId);

    /** Queues to one explicit address, bypassing contact resolution. */
    long queueEmail(EmailTemplateType template, EmailEntityType entityType, long entityId,
                String toAddress, String userId);

    List<Long> getQueuedEmailIds(int maxRows);

    void markSent(long emailId);

    void markFailed(long emailId, String reason);
}
