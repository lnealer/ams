CREATE OR REPLACE PACKAGE AMS_EMAIL_PG AS
  /*
   * Outbound notification queueing.
   *
   * Nothing here sends mail. A row is written to AMS_EMAIL_QUEUE inside the caller's transaction
   * and a separate poller delivers it afterwards, which is what makes a mail server outage unable
   * to roll back an order. The comment in OrderServiceImpl.submitOrder states the same intent from
   * the Java side.
   *
   * THIS PROCEDURE NEVER RAISES - deliberately, and unlike the eight scheduling procedures.
   *
   * It is the last call in submitOrder, cancelOrder and the NCR submit and cancel paths, all
   * inside @Transactional(readOnly = false). If it threw, a failure to queue a courtesy email
   * would roll back a perfectly good order. So the outermost handler swallows everything, rolls
   * back to its own savepoint and returns 'ERROR'. The eight scheduling procedures do the
   * opposite, because there a genuine fault SHOULD abandon the business transaction.
   */

  C_OK                  CONSTANT VARCHAR2(30) := 'OK';
  -- Recipients were found, but every one already had this mail inside the de-duplication window.
  C_SUPPRESSED          CONSTANT VARCHAR2(30) := 'SUPPRESSED';
  -- The customer has no active contact with a usable address for this kind of mail.
  C_NO_RECIPIENTS       CONSTANT VARCHAR2(30) := 'NO_RECIPIENTS';
  C_NOT_FOUND           CONSTANT VARCHAR2(30) := 'NOT_FOUND';
  C_INVALID_ENTITY_TYPE CONSTANT VARCHAR2(30) := 'INVALID_ENTITY_TYPE';
  C_INVALID_TEMPLATE    CONSTANT VARCHAR2(30) := 'INVALID_TEMPLATE';
  C_INVALID_INPUT       CONSTANT VARCHAR2(30) := 'INVALID_INPUT';
  C_ERROR               CONSTANT VARCHAR2(30) := 'ERROR';

  /*
   * Queues a templated notification against a business entity.
   *
   * Resolves recipients from the entity's customer contacts - which contact roles depends on the
   * entity type - and de-duplicates within a window taken from AMS_PROPERTIES key EMAILDEDUPMIN
   * (default 60 minutes).
   *
   * A suppressed duplicate is still written, with EMAIL_STATUS_CD = 'SUPPRESS', rather than
   * silently dropped. That way "why did the customer not get the mail" is answerable from the
   * table. EmailQueueStatusType.SUPPRESSED already carries that code and
   * EmailDetailDAOImpl.SELECT_QUEUED already ignores it, so the poller will not pick it up.
   *
   * p_email_id is the id of the first row actually queued, or null when everything was suppressed
   * or nobody could be resolved. EmailServiceImpl.queueEntityEmail returns it straight through and
   * no caller null-checks it, so null is safe.
   */
  PROCEDURE add_entity_email(p_entity_type_cd IN  VARCHAR2,
                             p_entity_id      IN  NUMBER,
                             p_template_cd    IN  VARCHAR2,
                             p_user_id        IN  VARCHAR2,
                             p_status_cd      OUT VARCHAR2,
                             p_email_id       OUT NUMBER);

END AMS_EMAIL_PG;
/
