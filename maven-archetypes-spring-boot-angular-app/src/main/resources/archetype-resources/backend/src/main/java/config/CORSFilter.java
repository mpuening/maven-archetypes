package ${groupId}.config;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Swagger UI gets a CORS error on the pre-flight call to the token end-point.
 *
 * We'll solve the problem for testing purposes by adding the
 * Access-Control-Allow-Origin header to the response.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Conditional(AuthorizationServerConfiguration.EnableAuthorizationServerCondition.class)
public class CORSFilter implements Filter {

	private final Set<String> allowedOrigins;

	public CORSFilter(@Value("#[[${spring.security.cors.allowed-origins:*}]]#") Set<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) resp;
		HttpServletRequest request = (HttpServletRequest) req;
		String origin = request.getHeader("referer");
		if (origin != null) {
			Optional<String> first = allowedOrigins.stream().filter(origin::startsWith).findFirst();
			first.ifPresentOrElse(s -> response.setHeader("Access-Control-Allow-Origin", s), () -> {
				if (allowedOrigins.contains("*")) {
					// Open the flood gates
					response.setHeader("Access-Control-Allow-Origin", "*");
				}
			});
		}
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers",
				"Content-Type, x-requested-with, authorization, Authorization, credential, X-XSRF-TOKEN");

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			chain.doFilter(req, resp);
		}
	}
}
