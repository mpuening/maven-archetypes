JAX-RS API
==========

This project is a simple JAX-RS application that includes a simple Ping service. The service
is protected by basic authentication. When running in test mode, the users are defined in
`TestCredentialValidator.java`.


Building and Running the Application
====================================

To build the application, run this command:

```
mvn clean package
```

To run the application within an application server, run one of these commands:

```
mvn -P liberty liberty:run
mvn -P wildfly cargo:run
mvn -P glassfish cargo:run
mvn -P tomee tomee:run
```

The UI will be accessible from this URL:

```
http://localhost:8080/${artifactId}/index.html
```

or if Open Liberty

```
http://localhost:9080/${artifactId}/index.html
```

From that page, you have three simple links to:

1. Ping the server
2. View the OpenAPI Spec
3. Use Swagger UI

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

Authentication Information
==========================
TODO