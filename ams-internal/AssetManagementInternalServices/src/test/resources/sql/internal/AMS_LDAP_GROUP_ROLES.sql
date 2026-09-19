-- Maps a directory group to an AMS role. Data rather than configuration, so a group can be granted
-- an existing role without an application release.
CREATE TABLE AMS_LDAP_GROUP_ROLES (
  LDAP_GROUP_NAME       VARCHAR(120) NOT NULL,
  ROLE_CD               VARCHAR(80) NOT NULL,
  ACTIVE_FL             CHAR(1) DEFAULT 'Y',
  PRIMARY KEY (LDAP_GROUP_NAME, ROLE_CD)
);
