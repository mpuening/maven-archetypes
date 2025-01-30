package ${groupId}.config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.TransformedResource;

@Configuration
public class WebAppConfiguration implements WebMvcConfigurer {

	private static final Logger log = LoggerFactory.getLogger(WebAppConfiguration.class);
	
	private static final String PATH_PATTERNS = "/**";
	private static final String INDEX_HTML = "index.html";
	private static final String API_PATH = "/api";

	private static final String DIRECTORY_SEPARATOR = "/";
	private static final String BASE_HREF_PLACEHOLDER = "#base-href#";

	private final ApplicationContext applicationContext;
	private final String[] staticLocations;
	private final String contextPath;

	public WebAppConfiguration(ApplicationContext applicationContext, WebProperties webProperties) {
		this.applicationContext = applicationContext;
		this.staticLocations = webProperties.getResources().getStaticLocations();
		this.contextPath = applicationContext.getEnvironment().getProperty("server.context-path");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry
				.addResourceHandler(PATH_PATTERNS)
				.addResourceLocations(staticLocations)
				.resourceChain(true)
				.addResolver(new MissingWebRouteResourceResolver());
	}

	private class MissingWebRouteResourceResolver extends PathResourceResolver {

		private final Resource indexHtmlResource;

		public MissingWebRouteResourceResolver() {
			this.indexHtmlResource = Arrays
					.stream(staticLocations)
					.map(path -> applicationContext.getResource(path + INDEX_HTML))
					.filter(this::doesResourceExistsAndIsReadable)
					.findFirst()
					.map(resource -> transformResource(resource))
					.orElseGet(() -> {
						log.warn(INDEX_HTML + " not found.");
						return null;
					});
		}

		@Override
		protected Resource getResource(String resourcePath, Resource location) throws IOException {
			var resource = location.createRelative(resourcePath);
			if (doesResourceExistsAndIsReadable(resource)) {
				if (resourcePath.endsWith(INDEX_HTML)) {
					return indexHtmlResource;
				}
				// Files such as images, js, css, etc.
				return resource;
			}

			// Don't interfere with the api path
			if ((DIRECTORY_SEPARATOR + resourcePath).startsWith(API_PATH)) {
				return null;
			}

			// Route is missing, so serve up index.html if present and let angular routing take over
			if (doesResourceExistsAndIsReadable(location.createRelative(INDEX_HTML))) {
				return indexHtmlResource;
			}

			return null;
		}

		private boolean doesResourceExistsAndIsReadable(Resource resource) {
			return resource.exists() && resource.isReadable();
		}

		private TransformedResource transformResource(Resource resource) {
			try {
				String fileContent = IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
				fileContent = fileContent.replace(BASE_HREF_PLACEHOLDER, contextPath + DIRECTORY_SEPARATOR);
				return new TransformedResource(resource, fileContent.getBytes(Charset.defaultCharset()));
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
}