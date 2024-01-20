package ${groupId}.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class H2SecurityConfiguration {

	@Bean
	@Order(3)
	public SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) throws Exception {
		return http
				.securityMatchers(matchers -> matchers.requestMatchers(antMatcher("/h2-console/**")))
				.csrf(csrf -> csrf.disable())
				.headers(headers -> headers.frameOptions(options -> options.sameOrigin()))
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.build();
	}
}
