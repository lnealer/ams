CREATE OR REPLACE PACKAGE BODY AMS_EMAIL_PG AS

  C_DEFAULT_DEDUP_MINUTES CONSTANT PLS_INTEGER := 60;

  FUNCTION get_dedup_minutes RETURN NUMBER IS
    l_value AMS_PROPERTIES.PROPERTY_VALUE%TYPE;
  BEGIN
    SELECT PROPERTY_VALUE INTO l_value
      FROM AMS_PROPERTIES WHERE PROPERTY_KEY = 'EMAILDEDUPMIN';
    RETURN TO_NUMBER(TRIM(l_value));
  EXCEPTION
    WHEN OTHERS THEN RETURN C_DEFAULT_DEDUP_MINUTES;
  END get_dedup_minutes;

  /*
   * Resolves the customer behind an entity.
   *
   * The five entity types reach a customer by different routes - an order carries one directly, an
   * RMA has to go through its asset - which is why this cannot be a single join.
   */
  FUNCTION resolve_customer(p_entity_type_cd IN VARCHAR2,
                            p_entity_id      IN NUMBER) RETURN NUMBER IS
    l_customer_id NUMBER;
  BEGIN
    CASE p_entity_type_cd
      WHEN 'ORDER' THEN
        SELECT CUSTOMER_ID INTO l_customer_id FROM AMS_ORDERS WHERE ORDER_ID = p_entity_id;
      WHEN 'NCR' THEN
        SELECT CUSTOMER_ID INTO l_customer_id
          FROM AMS_NETWORK_CHANGE_REQUESTS WHERE NCR_ID = p_entity_id;
      WHEN 'ASSET' THEN
        SELECT CUSTOMER_ID INTO l_customer_id FROM AMS_ASSETS WHERE ASSET_ID = p_entity_id;
      WHEN 'DECOM' THEN
        SELECT A.CUSTOMER_ID INTO l_customer_id
          FROM AMS_DECOMMISSIONS D
          JOIN AMS_ASSETS A ON A.ASSET_ID = D.ASSET_ID
         WHERE D.DECOMMISSION_ID = p_entity_id;
      WHEN 'RMA' THEN
        SELECT A.CUSTOMER_ID INTO l_customer_id
          FROM AMS_RMAS R
          JOIN AMS_ASSETS A ON A.ASSET_ID = R.ASSET_ID
         WHERE R.RMA_ID = p_entity_id;
      ELSE
        RETURN NULL;
    END CASE;
    RETURN l_customer_id;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN NULL;
  END resolve_customer;

  -- ------------------------------------------------------------------
  -- add_entity_email
  -- ------------------------------------------------------------------
  PROCEDURE add_entity_email(p_entity_type_cd IN  VARCHAR2,
                             p_entity_id      IN  NUMBER,
                             p_template_cd    IN  VARCHAR2,
                             p_user_id        IN  VARCHAR2,
                             p_status_cd      OUT VARCHAR2,
                             p_email_id       OUT NUMBER) IS
    l_type        VARCHAR2(20 CHAR) := UPPER(TRIM(p_entity_type_cd));
    l_template    VARCHAR2(20 CHAR) := UPPER(TRIM(p_template_cd));
    l_customer_id NUMBER;
    l_dedup_min   NUMBER;
    l_role_1      VARCHAR2(20 CHAR);
    l_role_2      VARCHAR2(20 CHAR);
    l_duplicates  NUMBER;
    l_queued      NUMBER := 0;
    l_suppressed  NUMBER := 0;
    l_new_id      NUMBER;
  BEGIN
    p_status_cd := C_ERROR;
    p_email_id  := NULL;

    SAVEPOINT ams_add_email;

    IF NVL(p_entity_id, 0) = 0 THEN
      p_status_cd := C_INVALID_INPUT;
      RETURN;
    END IF;
    IF l_type NOT IN ('ORDER', 'ASSET', 'NCR', 'RMA', 'DECOM') THEN
      p_status_cd := C_INVALID_ENTITY_TYPE;
      RETURN;
    END IF;
    IF l_template NOT IN ('ORDCONF', 'ORDCANCEL', 'INSTSCHED', 'NCRCONF',
                          'NCRCANCEL', 'DECOMSCHED', 'RMAREMIND') THEN
      p_status_cd := C_INVALID_TEMPLATE;
      RETURN;
    END IF;

    l_customer_id := resolve_customer(l_type, p_entity_id);
    IF l_customer_id IS NULL THEN
      p_status_cd := C_NOT_FOUND;
      RETURN;
    END IF;

    -- Which contact roles hear about which kind of event.
    CASE l_type
      WHEN 'ORDER' THEN l_role_1 := 'ORDERING'; l_role_2 := 'INSTALL';
      WHEN 'NCR'   THEN l_role_1 := 'ORDERING'; l_role_2 := 'TECH';
      WHEN 'ASSET' THEN l_role_1 := 'TECH';     l_role_2 := 'TECH';
      WHEN 'DECOM' THEN l_role_1 := 'ORDERING'; l_role_2 := 'TECH';
      WHEN 'RMA'   THEN l_role_1 := 'SHIPPING'; l_role_2 := 'TECH';
    END CASE;

    l_dedup_min := get_dedup_minutes;

    FOR c IN (SELECT DISTINCT LOWER(TRIM(EMAIL_ADDRESS)) AS ADDR
                FROM AMS_CONTACTS
               WHERE CUSTOMER_ID = l_customer_id
                 AND ACTIVE_FL = 'Y'
                 AND CONTACT_TYPE_CD IN (l_role_1, l_role_2)
                 AND EMAIL_ADDRESS IS NOT NULL
                 -- Cheapest possible sanity check. Real validation is the mail poller's job;
                 -- this only keeps obvious rubbish out of the queue.
                 AND INSTR(EMAIL_ADDRESS, '@') > 1) LOOP

      SELECT COUNT(*) INTO l_duplicates
        FROM AMS_EMAIL_QUEUE
       WHERE ENTITY_TYPE_CD = l_type
         AND ENTITY_ID = p_entity_id
         AND TEMPLATE_CD = l_template
         AND LOWER(TO_ADDRESS) = c.ADDR
         -- SUPPRESS is excluded on purpose, so one suppression cannot suppress the next.
         AND EMAIL_STATUS_CD IN ('QUEUED', 'SENDING', 'SENT')
         AND CREATED_DT > SYSTIMESTAMP - NUMTODSINTERVAL(l_dedup_min, 'MINUTE');

      l_new_id := AMS_EMAIL_QUEUE_SQ.NEXTVAL;

      IF l_duplicates > 0 THEN
        INSERT INTO AMS_EMAIL_QUEUE
               (EMAIL_ID, TEMPLATE_CD, ENTITY_TYPE_CD, ENTITY_ID, TO_ADDRESS,
                EMAIL_STATUS_CD, FAILURE_REASON, CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY)
        VALUES (l_new_id, l_template, l_type, p_entity_id, c.ADDR,
                'SUPPRESS', 'Duplicate within the ' || l_dedup_min || ' minute window',
                SYSTIMESTAMP, p_user_id, SYSTIMESTAMP, p_user_id);
        l_suppressed := l_suppressed + 1;
      ELSE
        INSERT INTO AMS_EMAIL_QUEUE
               (EMAIL_ID, TEMPLATE_CD, ENTITY_TYPE_CD, ENTITY_ID, TO_ADDRESS,
                EMAIL_STATUS_CD, CREATED_DT, CREATED_BY, MODIFIED_DT, MODIFIED_BY)
        VALUES (l_new_id, l_template, l_type, p_entity_id, c.ADDR,
                'QUEUED', SYSTIMESTAMP, p_user_id, SYSTIMESTAMP, p_user_id);
        l_queued := l_queued + 1;
        IF p_email_id IS NULL THEN
          p_email_id := l_new_id;
        END IF;
      END IF;
    END LOOP;

    IF l_queued > 0 THEN
      p_status_cd := C_OK;
    ELSIF l_suppressed > 0 THEN
      p_status_cd := C_SUPPRESSED;
    ELSE
      p_status_cd := C_NO_RECIPIENTS;
    END IF;
  EXCEPTION
    WHEN OTHERS THEN
      -- Swallowed on purpose. See the package header: a failure to queue a notification must not
      -- take down the order it was notifying about.
      ROLLBACK TO ams_add_email;
      p_status_cd := C_ERROR;
      p_email_id  := NULL;
  END add_entity_email;

END AMS_EMAIL_PG;
/
