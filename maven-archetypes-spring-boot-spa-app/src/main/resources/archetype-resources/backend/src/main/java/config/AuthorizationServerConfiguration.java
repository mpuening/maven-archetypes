package ${groupId}.config;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is the configuration for the OAuth2 Authorization Server. It uses the
 * auth provider that is configured in LDAPAuthConfiguration.
 *
 * It also customizes the access tokens, and relaxes issuing refresh tokens.
 *
 * Some code inspired from https://github.com/spring-projects/spring-authorization-server/pull/1432
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
	SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
			RegisteredClientRepository registeredClientRepository)
			throws Exception {

		// Ignore self signed cert issues in HttpsURLConnection for JWKSource retrieval
		ignoreSelfSignedCertIssues();

		return http
			.oauth2AuthorizationServer(authorizationServer -> {
				http.securityMatcher(authorizationServer.getEndpointsMatcher());
				authorizationServer
					.oidc(Customizer.withDefaults())
					.clientAuthentication(clientAuthentication -> clientAuthentication
						.authenticationConverter(new PublicClientRefreshTokenAuthenticationConverter())
						.authenticationProvider(new PublicClientRefreshTokenAuthenticationProvider(registeredClientRepository)));
			})
			.formLogin(Customizer.withDefaults())
			.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
					new LoginUrlAuthenticationEntryPoint("/login"),
					new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
			.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
			.build();
	}

	protected void ignoreSelfSignedCertIssues() {
		try {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}
			} }, new SecureRandom());
			javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Bean
	OAuth2TokenGenerator<?> tokenGenerator(JWKSource<SecurityContext> jwkSource) {
		JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
		jwtGenerator.setJwtCustomizer(jwtCustomizer());
		OAuth2TokenGenerator<OAuth2RefreshToken> refreshTokenGenerator = new CustomRefreshTokenGenerator();
		return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenGenerator);
	}

	private static class CustomRefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {
		private final StringKeyGenerator refreshTokenGenerator = new Base64StringKeyGenerator(
				Base64.getUrlEncoder().withoutPadding(), 96);

		@Override
		public OAuth2RefreshToken generate(OAuth2TokenContext context) {
			if (isPublicClientForAuthorizationCodeGrant(context)
					&& context.getAuthorizedScopes().contains(OidcScopes.OPENID)
					&& !context.getAuthorizedScopes().contains("offline_access")) {
				return null;
			}
			Instant issuedAt = Instant.now();
			Instant expiresAt = issuedAt.plus(context.getRegisteredClient().getTokenSettings().getRefreshTokenTimeToLive());
			OAuth2RefreshToken token = new OAuth2RefreshToken(this.refreshTokenGenerator.generateKey(), issuedAt, expiresAt);
			return token;
		}

		protected boolean isPublicClientForAuthorizationCodeGrant(OAuth2TokenContext context) {
			if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(context.getAuthorizationGrantType())
					&& (context.getAuthorizationGrant()
							.getPrincipal() instanceof OAuth2ClientAuthenticationToken clientPrincipal)) {
				return clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE);
			}
			return false;
		}
	}

	private static class PublicClientRefreshTokenAuthenticationToken extends OAuth2ClientAuthenticationToken {
		private static final long serialVersionUID = 1125278833961082403L;

		private PublicClientRefreshTokenAuthenticationToken(String clientId) {
			super(clientId, ClientAuthenticationMethod.NONE, null, null);
		}

		private PublicClientRefreshTokenAuthenticationToken(RegisteredClient registeredClient) {
			super(registeredClient, ClientAuthenticationMethod.NONE, null);
		}
	}

	private static class PublicClientRefreshTokenAuthenticationConverter implements AuthenticationConverter {
		@Override
		public Authentication convert(HttpServletRequest request) {
			// grant_type (REQUIRED)
			String grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
			if (!AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(grantType)) {
				return null;
			}
			// client_id (REQUIRED)
			String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
			if (!StringUtils.hasText(clientId)) {
				return null;
			}
			return new PublicClientRefreshTokenAuthenticationToken(clientId);
		}
	}

	private static class PublicClientRefreshTokenAuthenticationProvider implements AuthenticationProvider {
		private final RegisteredClientRepository registeredClientRepository;

		private PublicClientRefreshTokenAuthenticationProvider(RegisteredClientRepository registeredClientRepository) {
			Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
			this.registeredClientRepository = registeredClientRepository;
		}

		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {
			PublicClientRefreshTokenAuthenticationToken publicClientAuthentication = (PublicClientRefreshTokenAuthenticationToken) authentication;
			if (!ClientAuthenticationMethod.NONE.equals(publicClientAuthentication.getClientAuthenticationMethod())) {
				return null;
			}
			String clientId = publicClientAuthentication.getPrincipal().toString();
			RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
			if (registeredClient == null) {
				throwInvalidClient(OAuth2ParameterNames.CLIENT_ID);
			}
			if (!registeredClient.getClientAuthenticationMethods()
					.contains(publicClientAuthentication.getClientAuthenticationMethod())) {
				throwInvalidClient("authentication_method");
			}
			return new PublicClientRefreshTokenAuthenticationToken(registeredClient);
		}

		@Override
		public boolean supports(Class<?> authentication) {
			return PublicClientRefreshTokenAuthenticationToken.class.isAssignableFrom(authentication);
		}

		private static void throwInvalidClient(String parameterName) {
			OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT,
					"Public client authentication failed: " + parameterName, null);
			throw new OAuth2AuthenticationException(error);
		}
	}

	@Bean
	@Order(2)
	SecurityFilterChain loginSecurityFilterChain(HttpSecurity http) throws Exception {
		return http
				.securityMatcher("/login", "/error", "/default-ui.css")
				.formLogin(Customizer.withDefaults())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/error").permitAll()
						.requestMatchers("/default-ui.css").permitAll()
						.anyRequest().authenticated())
				.build();
	}

	/**
	 * Add role claim to access token, username and more onto id token
	 */
	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
		return context -> {
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				Authentication principal = context.getPrincipal();
				Set<String> authorities = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority)
						.map(role -> role.replace("ROLE_", "")).collect(Collectors.toSet());
				context.getClaims().claim("roles", authorities);
			} else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
				Authentication principal = context.getPrincipal();
				context.getClaims().claim("username", principal.getName());
				context.getClaims().claim("fullName", principal.getName());
			}
		};
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