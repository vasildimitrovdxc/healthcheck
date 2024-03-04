Compiling:

mvn clean package

Configuration path : /opt/props/healthcheck.conf | content syntax example:


# Example configuration file for healthcheck servlet

# Target URL to perform health check
targetUrl=https://<host:port>/<context>

# Timeout in seconds for health check
timeout=10


# healthcheck
