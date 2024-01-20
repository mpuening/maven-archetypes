package ${groupId}.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuration to route errors and exceptions related to APIs into @RestControllerAdvice
 *
 * This handles auth related exceptions too
 */
@Configuration
public class ErrorConfiguration {

	@Bean
	public FilterRegistrationBean<ExceptionHandlingFilter> errorHandingFilter(ApplicationContext applicationContext,
			HandlerExceptionResolver handlerExceptionResolver) {
		FilterRegistrationBean<ExceptionHandlingFilter> filterRegistrationBean = new FilterRegistrationBean<>(
				new ExceptionHandlingFilter(handlerExceptionResolver, findHandlerMappings(applicationContext)));
		filterRegistrationBean.setUrlPatterns(List.of("/api/*"));
		filterRegistrationBean.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER - 200);
		return filterRegistrationBean;
	}

	protected List<HandlerMapping> findHandlerMappings(ApplicationContext applicationContext) {
		Map<String, HandlerMapping> beans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
				HandlerMapping.class, true, false);
		List<HandlerMapping> handlerMappings = Collections.emptyList();
		if (beans != null && !beans.isEmpty()) {
			handlerMappings = new ArrayList<>(beans.values());
			AnnotationAwareOrderComparator.sort(handlerMappings);
		}
		return handlerMappings;
	}

	public static class ExceptionHandlingFilter implements Filter {
		private final HandlerExceptionResolver handlerExceptionResolver;
		private final List<HandlerMapping> handlerMappings;

		public ExceptionHandlingFilter(HandlerExceptionResolver handlerExceptionResolver,
				List<HandlerMapping> handlerMappings) {
			this.handlerExceptionResolver = handlerExceptionResolver;
			this.handlerMappings = handlerMappings;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			try {
				chain.doFilter(request, response);
			} catch (Exception ex) {
				HandlerExecutionChain handlerExecutionChain = getHandlerExecutionChain((HttpServletRequest) request);
				Object handler = handlerExecutionChain != null ? handlerExecutionChain.getHandler() : null;
				if (handler instanceof ResourceHttpRequestHandler) {
					// If handler is not supported, re-throw exception
					throw ex;
				}
				handlerExceptionResolver.resolveException((HttpServletRequest) request, (HttpServletResponse) response,
						handler, ex);
			}
		}

		protected HandlerExecutionChain getHandlerExecutionChain(HttpServletRequest request) {
			for (HandlerMapping handlerMapping : this.handlerMappings) {
				HandlerExecutionChain handlerExecutionChain = null;
				try {
					handlerExecutionChain = handlerMapping.getHandler(request);
				} catch (Exception ex) {
					// Is this right? What to do?
					// ex.printStackTrace();
				}
				if (handlerExecutionChain != null) {
					return handlerExecutionChain;
				}
			}
			return null;
		}
	}
}
