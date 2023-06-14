FROM openliberty/open-liberty:23.0.0.1-full-java17-openj9-ubi

COPY --chown=1001:0 target/${artifactId}-*.war /config/dropins/${artifactId}.war
COPY --chown=1001:0 src/main/liberty/config/server-docker.xml /config/server.xml

ARG REPO_DIR="target/${artifactId}-${version}/WEB-INF/lib"

COPY --chown=1001:0 $REPO_DIR/derby-*.jar /config/derby.jar
COPY --chown=1001:0 $REPO_DIR/derbyclient-*.jar /config/derbyclient.jar
COPY --chown=1001:0 $REPO_DIR/derbytools-*.jar /config/derbytools.jar
COPY --chown=1001:0 $REPO_DIR/derbyshared-*.jar /config/derbyshared.jar
COPY --chown=1001:0 $REPO_DIR/HikariCP-*.jar /config/HikariCP.jar
COPY --chown=1001:0 $REPO_DIR/slf4j-api-*.jar /config/slf4j-api.jar
COPY --chown=1001:0 target/${artifactId}-*-datasource.jar /config/${artifactId}-datasource.jar

RUN configure.sh
