package ${groupId}.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

/**
 * Configuration for an Authentication Provider using LDAP.
 */
@Configuration
public class LDAPAuthConfiguration {

	@Value("${app.security.ldap-url}")
	protected String ldapUrl;

	@Bean
	public AuthenticationProvider authenticationProvider() throws Exception {

		DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(ldapUrl);
		contextSource.afterPropertiesSet();

		BindAuthenticator auth = new BindAuthenticator(contextSource);
		auth.setUserDnPatterns(new String[] { "uid={0},ou=people" });
		auth.afterPropertiesSet();

		//
		// This is how we fetch the groups the person is in
		//
		DefaultLdapAuthoritiesPopulator groupsPopulator = new DefaultLdapAuthoritiesPopulator(contextSource, "ou=groups");
		groupsPopulator.setGroupSearchFilter("(uniqueMember={0})");
		groupsPopulator.setRolePrefix("GROUP_");

		LdapAuthenticationProvider ldapProvider = new LdapAuthenticationProvider(auth, groupsPopulator);
		ldapProvider.setUserDetailsContextMapper(new LdapUserDetailsMapper());

		return ldapProvider;
	}
}
