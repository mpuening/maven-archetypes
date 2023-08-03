package ${groupId}.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class WebConfiguration {

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers(

				"/webjars/**",

				"/assets/**",

				"/css/**",

				"/swagger-ui/**",

				"/v3/api-docs/**");
	}

}
