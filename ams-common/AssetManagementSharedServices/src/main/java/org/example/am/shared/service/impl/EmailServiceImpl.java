package org.example.am.shared.service.impl;

import java.util.List;
import org.example.am.shared.dao.EmailDetailDAO;
import org.example.am.shared.dao.StoredProcedureDAO;
import org.example.am.shared.domain.EmailEntityType;
import org.example.am.shared.domain.EmailQueueStatusType;
import org.example.am.shared.domain.EmailTemplateType;
import org.example.am.shared.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Queues templated notifications. Nothing here talks to a mail server: rows are written inside the
 * caller's transaction and delivered by a separate poller.
 */
@Service("emailService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class EmailServiceImpl implements EmailService {

    @Autowired
    private EmailDetailDAO emailDetailDAO;

    @Autowired
    private StoredProcedureDAO storedProcedureDAO;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Long queueEntityEmail(final EmailTemplateType template, final EmailEntityType entityType,
            final long entityId, final String userId) {
        return storedProcedureDAO.addEntityEmail(entityType == null ? null : entityType.getCode(),
                entityId, template == null ? null : template.getCode(), userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public long queueEmail(final EmailTemplateType template, final EmailEntityType entityType,
            final long entityId, final String toAddress, final String userId) {
        return emailDetailDAO.queueEmail(template, entityType, entityId, toAddress, userId);
    }

    @Override
    public List<Long> getQueuedEmailIds(final int maxRows) {
        return emailDetailDAO.getQueuedEmailIds(maxRows);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void markSent(final long emailId) {
        emailDetailDAO.updateStatus(emailId, EmailQueueStatusType.SENT, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void markFailed(final long emailId, final String reason) {
        emailDetailDAO.updateStatus(emailId, EmailQueueStatusType.FAILED, reason);
    }
}
