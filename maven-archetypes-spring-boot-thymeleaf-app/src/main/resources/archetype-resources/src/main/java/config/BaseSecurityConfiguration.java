package ${groupId}.config;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseSecurityConfiguration {

	public static final String[] CLIENT_URLS = new String[] {
			"/css/**",
			"/webjars/**",
			"/actuator/info",
			"/actuator/health",
			"/h2-console/**"
	};

	public static final String[] RESOURCE_URLS = new String[] {
			"/api/**"
	};

	/**
	 * Create a RequestMatcher to match URLs from the browser for use in an OAuth
	 * client or Web App
	 */
	protected static RequestMatcher browserRequestMatcher(List<String> excludedPatterns, String... includedPatterns) {
		// Use header content to detect text/html requests that browser sends
		ContentNegotiationStrategy contentNegotiationStrategy = new HeaderContentNegotiationStrategy();
		MediaTypeRequestMatcher textHtmlMatcher = new MediaTypeRequestMatcher(contentNegotiationStrategy,
				MediaType.TEXT_HTML);

		// Gather excludes in a list
		List<RequestMatcher> excludedPatternsRequestMatchers = new ArrayList<>();
		for (String pattern : excludedPatterns) {
			excludedPatternsRequestMatchers.add(new AntPathRequestMatcher(pattern));
		}
		// Negate the list to allow all them to pass to next filter
		NegatedRequestMatcher ignoredMatchers = new NegatedRequestMatcher(
				new OrRequestMatcher(excludedPatternsRequestMatchers));

		// And combine the two above
		List<RequestMatcher> matchers = new ArrayList<>();
		matchers.add(new AndRequestMatcher(textHtmlMatcher, ignoredMatchers));

		// Lastly add any patterns to include
		for (String pattern : includedPatterns) {
			matchers.add(new AntPathRequestMatcher(pattern));
		}

		return new OrRequestMatcher(matchers);
	}

	/**
	 * Create a request matcher for the provided patterns that include basic auth
	 * request headers
	 */
	protected static RequestMatcher basicAuthRequestMatcher(String... includedPatterns) {
		// Gather excludes in a list
		List<RequestMatcher> includedPatternsRequestMatchers = new ArrayList<>();
		for (String pattern : includedPatterns) {
			includedPatternsRequestMatchers.add(new AntPathRequestMatcher(pattern));
		}
		OrRequestMatcher includedMatchers = new OrRequestMatcher(includedPatternsRequestMatchers);

		RequestMatcher basicAuthRequestMatcher = (request) -> {
			String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
			return (authorization != null && authorization.startsWith("Basic "));
		};
		return new AndRequestMatcher(includedMatchers, basicAuthRequestMatcher);
	}

	protected static void handleErrorResponse(Exception ex, HttpStatus status, String message,
			HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException {
		response.setStatus(status.value());
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		OutputStream out = response.getOutputStream();
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> body = Map.of(
				"status", status.value(),
				"error", status.getReasonPhrase(),
				"message", message);
		mapper.writeValue(out, body);
		out.flush();
	}
}
