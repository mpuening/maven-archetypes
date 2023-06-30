JSP Application
===============

This project is a simple JSP application with an example servlet.

This application was tested against these application servers:

* Open Liberty
* WildFly
* GlassFish
* TomEE

The application includes JPA support with an embedded Derby Database. Flyway is also included as a
convenient way to create tables and test data.

Building and Running the Application
====================================

To build the application, run this command:

```
mvn clean package
```

To run the application within an application server, run one of these commands; its UI will
be available at its corresponding URL:

| Command | URL |
| --------------------------- | ----------------------------------- |
| mvn -P liberty liberty:run  | http://localhost:9080/${artifactId} |
| mvn -P wildfly cargo:run    | http://localhost:8080/${artifactId} |
| mvn -P glassfish cargo:run  | http://localhost:8080/${artifactId} |
| mvn -P tomee tomee:run      | http://localhost:8080/${artifactId} |

To build a Docker image, run this command:

```
mvn clean package && sudo docker build -t ${groupId}/${artifactId} .
```

To run the Docker image, run this command:

```
docker rm -f ${artifactId} || true && docker run -d -p 9080:9080 --name ${artifactId} ${groupId}/${artifactId}
```

Or on Rancher Desktop:

```
kubectl run ${artifactId} --image=${groupId}/${artifactId}:latest --image-pull-policy=Never --port=9080
kubectl port-forward pods/${artifactId} 9080:9080

# To view logs:
kubectl logs -f ${artifactId}

# To delete:
kubectl delete pod ${artifactId}
docker rmi ${groupId}/${artifactId}:latest
```

To add the Maven wrapper to this project, run this command:

```
mvn -N wrapper:wrapper
```

Database Information
====================

If one wants to use the included database in code, it would look like this:

```
@Inject
@AppDataSource
DataSource dataSource;
```

or

```
@PersistenceContext
EntityManager entityManager;
```

If one wants to remove database support, do the following:

* Remove the DataSource and Flyway code from the project
* Remove the `persistence.xml` file
* Remove the `library` and `dataSource` elements from both `server.xml` files
* Remove the `derby` properties and dependencies from `pom.xml`
* Remove the `derby` COPY steps from Dockerfile
