package ${groupId}.ping;

import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import ${groupId}.util.sql.AppDataSource;

@RolesAllowed({ "user", "admin" })
@Path("api/ping")
@Named
@ApplicationScoped
public class PingService {

	@Inject
	@AppDataSource
	DataSource dataSource;

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	@ConfigProperty(name = "greeting", defaultValue = "UNKNOWN")
	String greeting;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(description = "Ping Response")
	public Response ping() {
		Map<String, Object> data = Map.of(
				"greeting", greeting,
				"dataSource", dataSource != null,
				"entityManager", entityManager != null
		);
		return Response.status(Status.OK).entity(data).build();
	}

}