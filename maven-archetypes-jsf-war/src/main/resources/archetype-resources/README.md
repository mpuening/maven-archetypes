JSF Application
===============

This project is a simple JSF application that includes a login form and a UI styled 
with Bootstrap. A database is also configured.

The accounts are included in an in-memory Identity Store. There are three accounts: 
one with credentials `admin:password` and others with credentials `alice:password`
and `bob:password`.  More accounts can be added to the `InMemoryIdentityStore.java`
file.

Building and Running the Application
====================================

To build the application, run this command:

```
mvn clean package
```

To run the application with the Open Liberty Maven Plug-in, run this command:

```
mvn liberty:stop && mvn liberty:run
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
gradle libertyStop
```

Database Information
====================

If one wants to use the included database in code, it would look like this:

```
@javax.annotation.Resource(name="jdbc/appDataSource")
private javax.sql.DataSource dataSource;
```

or

```
@javax.persistence.PersistenceContext(name = "jdbc/appDataSource")
private javax.persistence.EntityManager entityManager;
```

If one wants to remove database support, do the following:

* Remove the `library` and `dataSource` elements from both `server.xml` files
* Remove the `ibm.web.bnd.xml` file
* Remove the `resource-ref` element from `web.xml`
* Remove the `persistence.xml` file
* Remove the `derby` properties and dependency from `pom.xml`
* Remove the `derby` COPY step from Dockerfile

Note: The driver is not listed as provided in `pom.xml` so that the `Dockerfile` has
a known place from which to copy the driver JAR file into the Docker image.
