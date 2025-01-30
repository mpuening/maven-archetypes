package ${groupId}.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is the configuration for the OAuth2 Authorization Server. It uses the
 * auth provider that is configured in LDAPAuthConfiguration.
 * 
 * It also configures a custom login page as well as customizing the access
 * tokens.
 */
@Configuration
@Controller
@Conditional(AuthorizationServerConfiguration.EnableAuthorizationServerCondition.class)
public class AuthorizationServerConfiguration {

	public static class EnableAuthorizationServerCondition implements Condition {
		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return context.getEnvironment().acceptsProfiles(Profiles.of("authserver"));
		}
	}

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
	 * Add role claim to access token, username and more onto id token
	 */
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
		return context -> {
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				Authentication principal = context.getPrincipal();
				Set<String> authorities = principal.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.map(role -> role.replace("ROLE_", ""))
						.collect(Collectors.toSet());
				context.getClaims().claim("roles", authorities);
			} else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
				Authentication principal = context.getPrincipal();
				context.getClaims().claim("username", principal.getName());
				context.getClaims().claim("fullName", principal.getName());
			}
		};
	}
	
	/**
	 * MSAL.js retrieves openid configuration on this URL. Until the time
	 * Spring's Auth server fully supports it, we'll just forward.
	 *
	 * https://127.0.0.1:8443/v2.0/.well-known/openid-configuration 
	 */
	@GetMapping("/v2.0/.well-known/openid-configuration")
	public void alternateConfiguration(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher("/.well-known/openid-configuration");
		rd.forward(request, response);
	}

	@GetMapping("/oauth2/logged-out")
	@ResponseBody
	public String loggedOut(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
		return """
		<!DOCTYPE html>
		<html>
		<body>
		<h1>You are logged out.</h1>
		<a href="https://localhost:8443">Visit app again.</a>
		</body>
		</html>""";
	}

}