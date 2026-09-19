Internal certificate authority
==============================

The image expects internal-ca.crt here - the public certificate of the CA that signs the reverse
proxy and the platform REST services. It is not committed, because which CA is in force differs per
environment.

The Dockerfile imports it into the JVM's cacerts. Without it, outbound calls to the platform
services fail the TLS handshake, which surfaces as the address validation service being reported as
unavailable.
