package ${groupId}.config;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

import ${groupId}.util.security.CredentialValidator;
import ${groupId}.util.security.CredentialValidatorChain;
import ${groupId}.util.security.ELConfiguredLDAPCredentialValidator;
import ${groupId}.util.security.TestCredentialValidator;

@DeclareRoles({ "admin", "user" })
@BasicAuthenticationMechanismDefinition
@ApplicationScoped
public class SecurityConfiguration implements IdentityStore {

	private CredentialValidator credentialValidator = new CredentialValidatorChain(
			new TestCredentialValidator(),
			new ELConfiguredLDAPCredentialValidator());

	@Override
	public CredentialValidationResult validate(Credential credential) {
		return credentialValidator.validate(credential);
	}

}
