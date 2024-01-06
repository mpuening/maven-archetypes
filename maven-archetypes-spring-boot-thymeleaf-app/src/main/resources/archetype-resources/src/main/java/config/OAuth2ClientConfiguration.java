package ${groupId}.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;

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
						login
							.loginPage("/oauth2/authorization/app-client")
							.tokenEndpoint(token -> token.accessTokenResponseClient(accessTokenResponseClient()))
				)
				.logout(logout ->
						logout
							.logoutRequestMatcher(antMatcher("/logout"))
							.logoutSuccessHandler(logoutSuccessHandler(clientRegistrationRepository))
				)
				.authorizeHttpRequests(authorize ->
						authorize

							.requestMatchers(antMatcher("/"), antMatcher("/home"), antMatcher("/logged-out")).permitAll()
	
							.requestMatchers(antMatcher("/webjars/**")).permitAll()
							.requestMatchers(antMatcher("/assets/**")).permitAll()
							.requestMatchers(antMatcher("/css/**")).permitAll()
							.requestMatchers(antMatcher("/swagger-ui/**")).permitAll()
							.requestMatchers(antMatcher("/v3/api-docs/**")).permitAll()
	
							.anyRequest().authenticated()
				)
				.build();
	}

	/**
	 * Creates an AccessTokenResponseClient that supports Azure AD SPA platforms. If
	 * you do not use Azure, then you can delete this method and use the default.
	 */
	private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
		RestTemplate restTemplate = new RestTemplate(
				Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
		restTemplate.setInterceptors(Arrays.asList(new ClientHttpRequestInterceptor() {
			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				if (request.getURI().getHost().contains("login.microsoftonline.com")) {
					// Azure AD wants ORIGIN header for SPA apps
					request.getHeaders().add(HttpHeaders.ORIGIN, "*");
				}
				return execution.execute(request, body);
			}
		}));
		restTemplate
				.setErrorHandler(new org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler());
		org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient();
		accessTokenResponseClient.setRestOperations(restTemplate);
		return accessTokenResponseClient;
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
