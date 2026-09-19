# ------------------------------------------------------------------------------
# AMS Internal Asset Management
#
# Deploys the WAR directly rather than the EAR. The EAR module is still built - it is how the
# application was installed into a shared application server before this - but a container runs one
# application, so the extra packaging layer buys nothing here.
# ------------------------------------------------------------------------------
# The specification names 26.0.0.2-full-java8-openj9-ubi-minimal. That tag does not exist, and
# neither does any java8+openj9 combination: IBM publishes Java 8 Liberty images only on IBM Java,
# and OpenJ9 variants start at Java 11. This is the closest real equivalent - same Liberty "full"
# feature set, same Java 8.
#
# Java 8 is kept rather than moving to a Java 11/17 image because the whole build targets it:
# maven.compiler.target=1.8, the jdbc-4.1 feature in server.xml, and ojdbc8 rather than ojdbc11.
# The cost is that no Java 8 Liberty image has an arm64 build, so on Apple Silicon this runs
# under emulation - see the platform pin in docker-compose.yml.
FROM websphere-liberty:26.0.0.8-full-java8-ibmjava

USER root

# ------------------------------------------------------------------------------
# Oracle JDBC driver.
#
# Copied in as a file rather than resolved from Maven Central: the runtime driver is
# ojdbc8-23.8.0.25.04, matching the Oracle 23ai server it talks to. It is newer than the 12.2.0.1 the
# build compiles and tests against, which is fine - the runtime driver and the test-scope one never
# have to agree. ojdbc8 rather than ojdbc11 because Liberty's jdbc-4.1 feature and the java8 base
# image both require it. server.xml picks it up as a shared library from this directory.
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
# The keystore path is detected rather than hard-coded. JAVA_HOME points at the JRE itself in this
# image (/opt/ibm/java/jre), so cacerts sits at $JAVA_HOME/lib/security - whereas on a JDK layout,
# and on Java 8 images where JAVA_HOME is the JDK root, it is under $JAVA_HOME/jre/lib/security.
# Guessing wrong fails the build with an unhelpful keytool error.
RUN set -eux; \
    CACERTS="$JAVA_HOME/lib/security/cacerts"; \
    [ -f "$CACERTS" ] || CACERTS="$JAVA_HOME/jre/lib/security/cacerts"; \
    [ -f "$CACERTS" ] || { echo "no cacerts under $JAVA_HOME"; exit 1; }; \
    keytool -importcert -noprompt -trustcacerts \
      -alias ams-internal-ca \
      -file /tmp/internal-ca.crt \
      -keystore "$CACERTS" \
      -storepass changeit; \
    rm -f /tmp/internal-ca.crt

# ------------------------------------------------------------------------------
# Liberty configuration.
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
# start-period is generous because on Apple Silicon this container is emulated (see the platform
# pin in docker-compose.yml) and Liberty takes considerably longer to open its port.
HEALTHCHECK --interval=30s --timeout=10s --start-period=180s --retries=5 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/9081 && \
      printf "GET /AssetManagementInternalWeb/health HTTP/1.0\r\n\r\n" >&3 && \
      grep -q "200" <&3' || exit 1
