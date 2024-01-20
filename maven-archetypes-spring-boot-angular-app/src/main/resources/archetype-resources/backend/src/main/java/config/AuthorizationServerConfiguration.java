package ${groupId}.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * This is the configuration for the OAuth2 Authorization Server. It uses the
 * auth provider that is configured in LDAPAuthConfiguration.
 * 
 * It also configures a custom login page as well as customizing the access
 * tokens.
 */
@Configuration
public class AuthorizationServerConfiguration {

	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
				.oidc(Customizer.withDefaults());
		return http
				.exceptionHandling(exceptions -> 
					exceptions.defaultAuthenticationEntryPointFor(
							new LoginUrlAuthenticationEntryPoint("/login"),
							new MediaTypeRequestMatcher(MediaType.TEXT_HTML))
				)
				.oauth2ResourceServer(oauth2 -> 
					oauth2.jwt(Customizer.withDefaults())
				)
				.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain loginSecurityFilterChain(HttpSecurity http) throws Exception {
		return http
				.securityMatcher("/login", "/error")
				.formLogin(Customizer.withDefaults())
				.authorizeHttpRequests(authorize -> 
						authorize
							.requestMatchers(antMatcher("/error")).permitAll()
							.anyRequest().authenticated()
				)
				.build();
	}
	
	/**
	 * Add a role claim and more to access token
	 */
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
		return context -> {
			if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
				Authentication principal = context.getPrincipal();
				Set<String> authorities = principal.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.map(role -> role.replace("ROLE_", ""))
						.collect(Collectors.toSet());
				context.getClaims().claim("roles", authorities);
				context.getClaims().claim("fullName", principal.getName());
			}
		};
	}
}