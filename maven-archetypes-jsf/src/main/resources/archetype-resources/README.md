JSF Application
===============

This project is a simple JSF application that includes a login form and a UI styled 
with Bootstrap.

The accounts are included in an in-memory Identity Store. There are two accounts: 
one with credentials `admin:password` and another with credentials `alice:password`. 
More accounts can add to the `InMemoryIdentityStore.java` file.

To build the application, run this command:

```
mvn clean package
```

To run the application with the Open Liberty Maven Plug-in, run this command:

```
mvn liberty:run
```

The UI will be accessible from this URL:

```
http:localhost:9080/${artifactId}
```

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
