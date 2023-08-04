package ${groupId}.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OAuth2 Resource Configuration for REST API auth
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class OAuth2ResourceConfiguration {

	@Bean
	@Order(4)
	public SecurityFilterChain oauth2ResourceSecurityFilterChain(HttpSecurity http) throws Exception {
		return http
				.securityMatchers(matchers -> matchers.requestMatchers(antMatcher("/api/**")))
				.oauth2ResourceServer(oauth2 ->
						oauth2
							.jwt(Customizer.withDefaults())
							.authenticationEntryPoint((req, res, ex) -> {
								// Handled via ErrorConfig and @RestControllerAdvice
								throw ex;
							})
							.accessDeniedHandler((req, res, ex) -> {
								// Handled via ErrorConfig and @RestControllerAdvice
								throw ex;
							})
				)
				.authorizeHttpRequests(authorize ->
						authorize
							.requestMatchers("/api/me").permitAll()
							.anyRequest().authenticated()
				)
				.build();
	}
}
