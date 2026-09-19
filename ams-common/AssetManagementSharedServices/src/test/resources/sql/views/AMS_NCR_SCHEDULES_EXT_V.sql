-- Flattens a network change request with its scheduled date and the asset it targets,
-- for the external scheduling feed.
CREATE VIEW AMS_NCR_SCHEDULES_EXT_V AS
SELECT N.NCR_ID,
       N.REQUEST_NUMBER,
       N.CUSTOMER_ID,
       N.ASSET_ID,
       A.ASSET_TAG,
       N.NCR_STATUS_CD,
       N.SCHEDULED_DT,
       N.REQUESTED_DT
  FROM AMS_NETWORK_CHANGE_REQUESTS N
  LEFT JOIN AMS_ASSETS A ON A.ASSET_ID = N.ASSET_ID;
