package ${groupId}.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("form-based-security")
public class FormBasedSecurityConfiguration extends BaseSecurityConfiguration {

	@GetMapping({ "/login" })
	public String login() {
		return "login";
	}

	@Autowired
	private DataSource dataSource;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JdbcUserDetailsManager jdbcUserDetailsManager(AuthenticationManager authenticationManager) throws Exception {
		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
		jdbcUserDetailsManager.setDataSource(dataSource);
		jdbcUserDetailsManager.setAuthenticationManager(authenticationManager);
		return jdbcUserDetailsManager;
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(this.dataSource)
				// Create dummy test users here
				.withUser(User.withUsername("admin").password(passwordEncoder().encode("password")).roles("ADMIN"))
				.withUser(User.withUsername("betty").password(passwordEncoder().encode("iloveyou")).roles("USER"))
				.withUser(User.withUsername("charlie").password(passwordEncoder().encode("123456")).roles("USER"));
	}

	@Order(2)
	@Configuration
	@Profile("form-based-security")
	public class BasicAuthSecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Bean
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}

		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity
					.requestMatchers().requestMatchers(basicAuthRequestMatcher(RESOURCE_URLS))
				.and()
					.authorizeRequests()
					.antMatchers("/api/me").permitAll()
					.anyRequest().authenticated()
				.and()
					.httpBasic()
				.and()
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
		}
	}

	@Order(3)
	@Configuration
	@Profile("form-based-security")
	public class WebAppSecurityConfiguration extends WebSecurityConfigurerAdapter {

		@Override
		protected void configure(HttpSecurity httpSecurity) throws Exception {
			httpSecurity
					.requestMatcher(
							new NegatedRequestMatcher(new RequestHeaderRequestMatcher(HttpHeaders.AUTHORIZATION)))
					.authorizeRequests()
					.antMatchers("/css/**", "/webjars/**").permitAll()
					.antMatchers("/actuator/info", "/actuator/health").permitAll()
					.antMatchers("/api/me").permitAll()
					.antMatchers("/admin/**").hasAnyRole("ADMIN")
					.antMatchers("/h2-console/**").hasAnyRole("ADMIN")
					.anyRequest().authenticated()
				.and()
					.formLogin().loginPage("/login").permitAll()
				.and()
					.logout().logoutSuccessUrl("/goodbye").permitAll();
			httpSecurity.csrf().ignoringAntMatchers("/h2-console/**");
			httpSecurity.headers().frameOptions().sameOrigin();
		}
	}
}
