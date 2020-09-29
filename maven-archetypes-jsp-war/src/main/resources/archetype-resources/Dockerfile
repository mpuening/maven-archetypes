FROM open-liberty:19.0.0.9-webProfile8-java11

COPY --chown=1001:0 target/${artifactId}-${version}.war /config/dropins/${artifactId}.war
COPY --chown=1001:0 src/main/liberty/config/server-docker.xml /config/server.xml

ARG REPO_DIR="target/${artifactId}-${version}/WEB-INF/lib"
ARG DERBY_VERSION="10.14.2.0"
COPY --chown=1001:0 $REPO_DIR/derby-$DERBY_VERSION.jar /config/derby.jar

RUN configure.sh
