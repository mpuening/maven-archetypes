package ${groupId}.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

//@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class OAuth2SecurityConfiguration extends BaseSecurityConfiguration {

	@Order(2)
	@Configuration
	@Profile("oauth2-security")
	public static class OAuth2ClientSecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Value("${application.oauth2-login-entry-point}")
		protected String loginEntryPoint;

		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity
					.requestMatcher(browserRequestMatcher(List.of(RESOURCE_URLS), CLIENT_URLS))
					.authorizeRequests()
					.antMatchers("/css/**", "/webjars/**").permitAll()
					.antMatchers("/actuator/info", "/actuator/health").permitAll()
					.antMatchers("/admin/**").hasAnyRole("ADMIN")
					.antMatchers("/h2-console/**").hasAnyRole("ADMIN")
					.anyRequest().authenticated()
				.and()
					.exceptionHandling(e -> e.authenticationEntryPoint(
							new LoginUrlAuthenticationEntryPoint(loginEntryPoint)))
					.oauth2Login()
				.and()
					.logout().logoutSuccessUrl("/goodbye").permitAll();
			httpSecurity.csrf().ignoringAntMatchers("/h2-console/**");
			httpSecurity.headers().frameOptions().sameOrigin();
		}
	}

	@Order(3)
	@Configuration
	@Profile("oauth2-security")
	public static class OAuth2ResourceSecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity
					.requestMatchers().antMatchers(RESOURCE_URLS)
				.and()
					.authorizeRequests()
					.antMatchers("/api/me").permitAll()
					.anyRequest().authenticated()
				.and()
					.oauth2ResourceServer().jwt()
				.and().and()
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				.and()
					.exceptionHandling(e -> {
						e.authenticationEntryPoint((request, response, ex) -> {
							handleErrorResponse(ex, HttpStatus.UNAUTHORIZED,
									"Full authentication is required to access this resource", response);
						});
						e.accessDeniedHandler((request, response, ex) -> {
							handleErrorResponse(ex, HttpStatus.FORBIDDEN,
									"Insufficient permissions to access this resource", response);
						});
					});
		}
	}
}
