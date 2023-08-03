package ${groupId}.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

	@Autowired
	OAuth2AuthorizedClientService clientService;

	@RequestMapping("/")
	public String root() {
		return "redirect:/home";
	}

	@RequestMapping("/home")
	public String home() {
		return "home";
	}

	@RequestMapping("/admin")
	public String admin() {
		return "admin";
	}

	@RequestMapping("/goodbye")
	public String goodbye() {
		return "goodbye";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/logged-out")
	public String loggedOut() {
		return "logged-out";
	}

	@GetMapping("/tokens")
	public String tokens(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
			OidcUser user = (OidcUser)(authentication.getPrincipal());
			String idToken = user.getIdToken().getTokenValue();
			System.out.println("IdToken: " + idToken);
			OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("app-client", authentication.getName());
			if (client != null) {
				String accessToken = client.getAccessToken().getTokenValue();
				System.out.println("AccessToken: " + accessToken);
			}
		}
		return "home";
	}
}
