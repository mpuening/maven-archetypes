package ${groupId}.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * OAuth2 Client Configuration for application authentication
 */
@Configuration
public class OAuth2ClientConfiguration {

	@Value("${app.security.end-session-endpoint}")
	protected String endSessionEndpoint;

	@Bean
	@Order(5)
	public SecurityFilterChain oauth2ClientSecurityFilterChain(HttpSecurity http,
			ClientRegistrationRepository clientRegistrationRepository) throws Exception {
		return http
				.oauth2Client(Customizer.withDefaults())
				.oauth2Login(login ->
						login.loginPage("/oauth2/authorization/app-client")
				)
				.logout(logout ->
						logout
							.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
							.logoutSuccessHandler(logoutSuccessHandler(clientRegistrationRepository))
				)
				.authorizeHttpRequests(authorize ->
				authorize

						.requestMatchers("/", "/home", "/logged-out").permitAll()

						.requestMatchers("/webjars/**").permitAll()
						.requestMatchers("/assets/**").permitAll()
						.requestMatchers("/css/**").permitAll()
						.requestMatchers("/swagger-ui/**").permitAll()
						.requestMatchers("/v3/api-docs/**").permitAll()

						.anyRequest().authenticated()
				)
				.build();
	}

	private LogoutSuccessHandler logoutSuccessHandler(
			ClientRegistrationRepository clientRegistrationRepository) {
		OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler =
				new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
		oidcClientInitiatedLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/logged-out");
		return oidcClientInitiatedLogoutSuccessHandler;
	}

	/**
	 * We need to customize the ClientRegistrationRepository to support end_session_endpoint
	 */
	@Bean
	public InMemoryClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
		List<ClientRegistration> registrations = new ArrayList<>(
				new OAuth2ClientPropertiesMapper(properties).asClientRegistrations().values());
		List<ClientRegistration> customRegistrations = new ArrayList<>();
		registrations.forEach(registration -> {
				// Spring only supports OidcClientInitiatedLogoutSuccessHandler's when issuer
				// URLs are set because end_session_endpoint does not get configured otherwise.
				// So we set that here.
				ClientRegistration.Builder custom = ClientRegistration.withClientRegistration(registration);
				custom.providerConfigurationMetadata(Map.of("end_session_endpoint", endSessionEndpoint));
				customRegistrations.add(custom.build());
			});
		return new InMemoryClientRegistrationRepository(customRegistrations);
	}
}
