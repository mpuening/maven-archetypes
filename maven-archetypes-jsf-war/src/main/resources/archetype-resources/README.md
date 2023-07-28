${artifactId} JSF App
=====================

This project is a simple JSF application that includes a few simple pages.

This application was tested (to varying levels of success) against these application servers:

* Open Liberty
* WildFly
* GlassFish
* TomEE

The application includes JPA support with an embedded Derby Database. Flyway is also included as a
convenient way to create tables and test data.

The application is protected by form based authentication. There is also a test mode.
When running in test mode, the users are defined in `TestCredentialValidator` and are:

| Username | Password |
| -------- | -------- |
| admin    | password |
| alice    | password |
| bob      | password |

When not using the test mode, the application should be configured to bind to an LDAP system.

Building and Running the Application
====================================

To build the application, run this command:

```
mvn clean package
```

To run the application within an application server, run one of these commands; its UI will
be available at its corresponding URL:


| Command | URL |
| --------------------------- | ----------------------------------------------- |
| mvn -P liberty liberty:run  | http://localhost:9080/${artifactId}/index.xhtml |
| mvn -P wildfly cargo:run    | http://localhost:8080/${artifactId}/index.xhtml |
| mvn -P glassfish cargo:run  | http://localhost:8080/${artifactId}/index.xhtml |
| mvn -P tomee tomee:run      | http://localhost:8080/${artifactId}/index.xhtml |

The UI has a simple Bootstrap layout.

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

* Remove the DataSource and Flyway code from the project (`util.sql` package)
* Remove the `DataSourceConfiguration` code
* Remove the `persistence.xml` file
* Remove the Flyway `db` files
* Remove the `library` and `dataSource` elements from both `server.xml` files
* Remove the `derby` properties and dependencies from `pom.xml`
* Remove the `derby` COPY steps from Dockerfile


Authentication Information
==========================

The `ApplicstionConfiguration` provides the support for form-based authentication. In typical fashion though,
it delegates the authentication to the `AppIdentityStore`. This application includes two identity stores.
One is a typical LDAP Identity Store called `ELConfiguredLDAPCredentialValidator`. This class uses EL configuration
to know what system to connect to. An example LDAP server that fits the default settings can be found here:

```
https://github.com/mpuening/learn-jakartaee/tree/master/learn-jakartaee-ldap-server
```

The other identity store included in this application is a test store. This is convenient for test cases and
running locally. It is enabled where the `TEST_USERS_ENABLED` environment variable or `test.users.enabled`
system property is set to "true". This flag is set to true when running the application using the maven
commands documented above (see `pom.xml`, `server.env` and `wildfly.cli`).

If one wants to remove security support, do the following:

* Remove the security utility code from the project (`util.security` package)
* Remove the authentication code from project (`auth` package)
* Remove the security configuration from `web.xml`
* Remove the `login.xhtml` page

Test Cases
==========

This application uses Arquillian and embedded Apache TomEE for test cases. The example test cases
show examples using a web driver to load pages.

Note that the default configuration uses port 8080 to run the embedded TomEE server. If one already
has an application server running on port 8080, funny results can occur. The embedded server does not
cause the test cases to fail when the port is in use. The test case continue to load pages running
on whatever server happens to be running on port 8080. Be mindful of this.

Also note that the test cases need Java module settings configured to get TomEE running properly.
There are notes in the test cases should one want to run the test cases in an IDE.

Questionable Information
========================

This section documents the many quirks and design decisions made in this application to obtain the
goal of having the same WAR file work on every supported application server. Because there are still many
ways applications servers are different, this leads to some funny looking code.

The following sections reference code that should be reviewed from time to time to see if the *glitch* is
still required. If one searches for "QUESTIONABLE" in the code, he or she can find the places in the
code that are discussed below.

**Hikari-based DataSource**

Getting a `DataSource` configured and customized using system properties or environment variables *and* working
consistently across all supported application servers is a seemingly impossible task using just the Jakarta EE
specification. The differences in the implementations of the application servers get in the way. But as the old
adage says, everything can be solved by another level of indirection. And that is what this application has with
`AbstractConfiguredDataSource` and `MPConfiguredDataSource`. These classes pull properties from
Micro-profile Config and smooth over all the various differences of the supported application servers. The
`DataSourceConfiguration` class uses that with an Hikari DataSource. What ends up happening though is that
there is potentially a *connection-pool-enabled* DataSource that sits on top of a *connection-pool-enabled* DataSource.
Is that a problem?

**FlyWay Migration**

Including FlyWay migrations inside an application is a bad idea because one inevitably deploys the application
in a cluster with multiple instances. And then you have trouble. But the convenience for local development
is high. This application should probably be upgraded to include a flag to indicate if a FlyWay migration
should occur.

**Java Module Settings**

Some application servers need Java module settings to run properly from Maven or running test cases. These
module settings should be reviewed from time to time to see what is required.
