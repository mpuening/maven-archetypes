Spring Boot Thymeleaf Application
=================================

This project is a simple Spring Boot application that includes a login form and a UI styled 
with Bootstrap. A database is also configured.

There are two types of security: Form Based and OAuth2. The former is enabled by default.
Two Spring profiles can be used to enable them: `form-based-security` and `oauth2-security`.
For form based security, default users are configured in `FormBasedSecurityConfiguration`.
For OAuth2 security, the `application.yml` file contains the OAuth2 configuration. 
As is, the configuration is for the ORY Hydra as implemented
[here](https://github.com/mpuening/learn-hydra-login-and-consent).


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
mvn -N io.takari:maven:0.7.7:wrapper
```

For gradle users, these are useful commands:

```
gradle build
gradle bootRun
```

