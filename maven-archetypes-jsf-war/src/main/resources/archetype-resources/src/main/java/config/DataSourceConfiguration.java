package ${groupId}.config;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;

import ${groupId}.util.flyway.FlywayMigration;
import ${groupId}.util.sql.AppDataSource;

/**
 * Use EL properties to connect to database.
 */
@DataSourceDefinition(
		name = "java:app/env/jdbc/appDataSource",
		className = "${groupId}.util.sql.ELConfiguredDataSource",
		url = "not empty env.get('DB_URL') ? env.get('DB_URL') : "
				+ "properties.getOrDefault('db.url', 'jdbc:derby:memory:appdb%3Bcreate=true')",
		user = "not empty env.get('DB_USERNAME') ? env.get('DB_USERNAME') : "
				+ "properties.getOrDefault('db.user', 'APP') / "
				+ "not empty env.get('DB_PASSWORD') ? env.get('DB_PASSWORD') : "
				+ "properties.getOrDefault('db.password', '')",
		password = "not empty env.get('DB_PASSWORD') ? env.get('DB_PASSWORD') : "
				+ "properties.getOrDefault('db.password', '')",
		properties = {
				"driverClassName=not empty env.get('DB_DRIVER') ? env.get('DB_DRIVER') : "
						+ "properties.getOrDefault('db.driver', 'org.apache.derby.jdbc.EmbeddedDriver')"
		})
@ApplicationScoped
public class DataSourceConfiguration {

	@Resource(lookup = "java:app/env/jdbc/appDataSource")
	DataSource dataSource;

	@Produces
	@AppDataSource
	public DataSource getDatasource() {
		return dataSource;
	}

	/**
	 * QUESTIONABLE Should use only for local development
	 */
	public void onStart(@Observes @Initialized(ApplicationScoped.class) Object unused) {
		// It's not good practice to have an app be responsible to run migrations.
		// But it is quite convenient.
		FlywayMigration.run(dataSource, DataSourceConfiguration.class.getClassLoader());
	}
}