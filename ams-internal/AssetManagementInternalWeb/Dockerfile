# ------------------------------------------------------------------------------
# AMS Internal Asset Management
#
# Deploys the WAR directly rather than the EAR. The EAR module is still built - it is how the
# application was installed into a shared application server before this - but a container runs one
# application, so the extra packaging layer buys nothing here.
# 
# Updated for Java 21 with Jakarta EE 9+ support.
# The Java 21 Liberty image supports both amd64 and arm64 platforms natively.
# ------------------------------------------------------------------------------
FROM websphere-liberty:latest-java21-full

USER root

# ------------------------------------------------------------------------------
# Oracle JDBC driver.
#
# Copied in as a file rather than resolved from Maven Central: the runtime driver is
# ojdbc8-23.8.0.25.04, matching the Oracle 23ai server it talks to.
# ojdbc8 supports Java 21 and works with Jakarta EE 9+.
# server.xml picks it up as a shared library from this directory.
# ------------------------------------------------------------------------------
COPY --chown=1001:0 lib/ojdbc8-23.8.0.25.04.jar /config/configDropins/lib/

# ------------------------------------------------------------------------------
# Internal certificate authority.
#
# The reverse proxy and the platform REST services present certificates signed by an internal CA,
# which no public trust store knows about. Importing it into the JVM's cacerts is what lets the
# outbound calls complete; without it every one fails the TLS handshake.
# ------------------------------------------------------------------------------
COPY --chown=1001:0 certs/internal-ca.crt /tmp/internal-ca.crt
# On Java 21, JAVA_HOME points to the JDK root, so cacerts is at $JAVA_HOME/lib/security
RUN set -eux; \
    CACERTS="$JAVA_HOME/lib/security/cacerts"; \
    [ -f "$CACERTS" ] || { echo "no cacerts under $JAVA_HOME"; exit 1; }; \
    keytool -importcert -noprompt -trustcacerts \
      -alias ams-internal-ca \
      -file /tmp/internal-ca.crt \
      -keystore "$CACERTS" \
      -storepass changeit; \
    rm -f /tmp/internal-ca.crt

# ------------------------------------------------------------------------------
# Liberty configuration.
# Updated for Jakarta EE 9+ features (servlet-6.0, pages-3.1, etc.)
# ------------------------------------------------------------------------------
COPY --chown=1001:0 src/main/liberty/config/server.xml            /config/
COPY --chown=1001:0 src/main/liberty/config/jvm.options           /config/
COPY --chown=1001:0 src/main/liberty/config/bootstrap.properties  /config/

RUN mkdir -p /logs && chown -R 1001:0 /logs && chmod -R g+rw /logs

# ------------------------------------------------------------------------------
# The application. Copied last: it is the layer that changes on every build, so everything above
# it stays cached.
# ------------------------------------------------------------------------------
COPY --chown=1001:0 target/AssetManagementInternalWeb.war /config/apps/

USER 1001

EXPOSE 9081 9444

# Warms the shared class cache and validates the configuration at build time rather than on the
# first request in production.
RUN configure.sh

# The health endpoint is permitted without authentication precisely so this can reach it.
#
# Written against bash's /dev/tcp rather than curl: this image ships neither curl nor wget, so the
# obvious version of this check fails with "curl: not found" and reports the container unhealthy
# forever while the application is running perfectly.
#
# start-period is reasonable for Java 21 Liberty which starts faster than Java 8.
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/9081 && \
      printf "GET /AssetManagementInternalWeb/health HTTP/1.0\r\n\r\n" >&3 && \
      grep -q "200" <&3' || exit 1
