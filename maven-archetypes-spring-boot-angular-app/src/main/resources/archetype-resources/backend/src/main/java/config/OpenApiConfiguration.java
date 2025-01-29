package ${groupId}.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;

@OpenAPIDefinition(
		info = @Info(title = "Application API", version = "1.0.0"), 
		security = {
				@SecurityRequirement(name = "oauth2")})
@SecuritySchemes(value = {
		@SecurityScheme(name = "oauth2", type = SecuritySchemeType.OAUTH2,
				flows = @OAuthFlows(authorizationCode = 
					@OAuthFlow(
							authorizationUrl = "${app.swagger.oauth2.authorization-uri}", 
							tokenUrl = "${app.swagger.oauth2.token-uri}",
							scopes = {
								@OAuthScope(name = "openid", description = "OpenID"),
								@OAuthScope(name = "profile", description = "Profile")})))})
@Configuration
public class OpenApiConfiguration {
}
