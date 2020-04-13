JAX-RS API
==========

This project is a simple JAX-RS application that includes a simple Ping service.

To build the application, run this command:

```
mvn clean package
```

To run the application with the Open Liberty Maven Plug-in, run this command:

```
mvn liberty:stop && mvn liberty:run
```

Once Liberty has started open your browser to [index.html](http://localhost:9080/${artifactId}/index.html)

From that page, you have three simple links to:

1. [Ping the server](http://localhost:9080/${artifactId}/api/ping)
2. [View the OpenAPI Spec](http://localhost:9080/openapi)
3. [Use Swagger UI](http://localhost:9080/openapi/ui)

The `ping` service is implemented in the `PingService` class and the `/api`
context path is controlled by the `JAXRSConfiguration`.

To build a Docker image, run this command:

```
mvn clean package && sudo docker build -t ${groupId}/${artifactId} .
```

To run the Docker image, run this command:

```
docker rm -f ${artifactId} || true && docker run -d -p 9080:9080 --name ${artifactId} ${groupId}/${artifactId}

```

To add the Maven wrapper to this project, run this command:

```
mvn -N io.takari:maven:0.7.7:wrapper
```

For gradle users, these are useful commands:

```
gradle build
gradle libertyRun
```
