package ${groupId}.api;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
public class MeController {

	@GetMapping("/api/me")
	public Data<Map<String, Object>> me(Authentication authentication) {
		Data<Map<String, Object>> response = Data.from(Map.of("anonymous", true));
		if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
			Jwt token = (Jwt) authentication.getPrincipal();
			System.out.println("Resource received token: " + token.getTokenValue());
			response = Data.from(Map.of(

					"sub", token.getSubject(),

					"aud", token.getAudience(),

					"fullName", Optional.ofNullable(token.getClaim("fullName")).orElse(""),

					"roles", Optional.ofNullable(token.getClaimAsStringList("roles")).orElse(Collections.emptyList()),

					"expiresIn", (token.getExpiresAt().toEpochMilli() - System.currentTimeMillis()) / 1000

			));
		} else if (authentication != null && authentication instanceof OAuth2AuthenticationToken) {
			response = Data.from(Map.of(
					"name", authentication.getName()
			));
		}
		return response;
	}

	@SecurityRequirement(name = "oauth2")
	@GetMapping("/api/me2")
	public Data<Map<String, Object>> me2(Authentication authentication) {
		return me(authentication);
	}

	@SecurityRequirement(name = "oauth2")
	@GetMapping("/api/me3")
	@PreAuthorize("hasRole('ORACLE')")
	public Data<Map<String, Object>> me3(Authentication authentication) {
		// No one should be able to invoke this method due to its permission check
		return me(authentication);
	}
}
