FROM open-liberty:19.0.0.9-microProfile3-java11
COPY --chown=1001:0  target/${artifactId}-${version}.war /config/dropins/${artifactId}.war
COPY --chown=1001:0  src/main/liberty/config/server-docker.xml /config/server.xml
RUN configure.sh
