package ${groupId}.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import jakarta.enterprise.context.ApplicationScoped;

import ${groupId}.util.flyway.FlywayMigration;

@Readiness
@ApplicationScoped
public class ReadyCheck implements HealthCheck {

	@Override
	public HealthCheckResponse call() {
		String name = "JAX-RS API Ready Check";
		boolean flywayComplete = FlywayMigration.isFlywayMigrationComplete();
		boolean isUp = flywayComplete;
		return HealthCheckResponse
				.named(name)
				.withData("flyway", flywayComplete)
				.status(isUp)
				.build();
	}
}
