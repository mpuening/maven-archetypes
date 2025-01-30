package ${groupId}.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Configuration
@EnableConfigurationProperties(AppConfiguration.AppSecurityProperties.class)
public class AppConfiguration {

	@Autowired
	private AppConfiguration.AppSecurityProperties appSecurityProperties;

	/**
	 * Provides the frontend Angular App with the values to configure itself.
	 */
	@GetMapping("/api/config")
	public AppConfiguration.AppSecurityProperties config() {
		return appSecurityProperties;
	}

	@ConfigurationProperties(prefix = "app.security")
	public static class AppSecurityProperties {
		private String authority;
		private List<String> knownAuthorities = new ArrayList<>();
		private String protocolMode;
		private String clientId;
		private List<String> scopes = new ArrayList<>();
		private String redirectUri;
		private String postLogoutRedirectUri;

		public String getAuthority() {
			return authority;
		}

		public void setAuthority(String authority) {
			this.authority = authority;
		}

		public List<String> getKnownAuthorities() {
			return knownAuthorities;
		}

		public void setKnownAuthorities(List<String> knownAuthorities) {
			this.knownAuthorities = knownAuthorities;
		}

		public String getProtocolMode() {
			return protocolMode;
		}

		public void setProtocolMode(String protocolMode) {
			this.protocolMode = protocolMode;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public List<String> getScopes() {
			return scopes;
		}

		public void setScopes(List<String> scopes) {
			this.scopes = scopes;
		}

		public String getRedirectUri() {
			return redirectUri;
		}

		public void setRedirectUri(String redirectUri) {
			this.redirectUri = redirectUri;
		}

		public String getPostLogoutRedirectUri() {
			return postLogoutRedirectUri;
		}

		public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
			this.postLogoutRedirectUri = postLogoutRedirectUri;
		}
	}
}
