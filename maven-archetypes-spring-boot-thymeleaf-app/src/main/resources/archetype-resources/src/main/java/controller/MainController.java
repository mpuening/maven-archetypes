package ${groupId}.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

	@RequestMapping({ "/", "/home" })
	public String home(Authentication authentication, Model model) {
		return "home";
	}

	@RequestMapping("/admin")
	public String admin(Authentication authentication, Model model) {
		return "admin";
	}

	@RequestMapping("/goodbye")
	public String goodbye() {
		return "goodbye";
	}

	@GetMapping("/api/me")
	@ResponseBody
	public Object me(Authentication authentication) {
		// @AuthenticationPrincipal OAuth2User principal
		if (authentication == null || !authentication.isAuthenticated()) {
			return Map.of("anonymous", true);
		}
		else {
			return authentication.getPrincipal();
		}
	}
	
	@GetMapping("/api/me2")
	@ResponseBody
	public Object me2(Authentication authentication) {
		return me(authentication);
	}
}
