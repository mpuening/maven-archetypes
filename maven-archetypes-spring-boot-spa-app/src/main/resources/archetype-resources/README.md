Spring Boot Angular Application
===============================

This project is a Spring Boot application that bizarrely combines all the following
into a single application:

* Angular Web Application
* LDAP Server
* Authorization (OAuth2) Server (binds to LDAP server above)
* OAuth2 Resource Server

Of course, a real application would not include all of them, but this application
is a great starting place for learning all the various components.

The LDAP server has the following people in it that one uses to log into the application:

| Username | Password       |
| -------- | -------------- |
| pat      | patspassword   |
| alice    | alicespassword |
| bob      | bobspassword   |


Building and Running the Application
====================================

To build the application, run this command:

```
mvn clean package
```

To run the application, run this command:

```
mvn spring-boot:run
```

The UI will be accessible from this URL:

```
http:localhost:8080
```

To build a Docker image, run this command:

```
mvn clean install spring-boot:build-image
```

To run the Docker image, run this command:

```
docker rm -f ${artifactId} || true && docker run -d -p 8080:8080 --name ${artifactId} ${artifactId}:${version}

```

To add the Maven wrapper to this project, run this command:

```
mvn -N wrapper:wrapper
```

For gradle users, these are useful commands:

```
gradle build
gradle bootRun
```

TODO
====

Here is a list of a few items left to do to improve app:

* OpenAPI Schema Responses (e.g. Data, see JAX-RS project)
* OpenAPI Operation Status Codes (401 / 403 / Customizer?)
* Remove stack trace in `ProblemDetail` (e.g. `curl http://localhost:8080/api/blah`)

