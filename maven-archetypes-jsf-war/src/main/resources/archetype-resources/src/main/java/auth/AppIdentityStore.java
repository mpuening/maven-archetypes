package ${groupId}.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

import ${groupId}.util.security.identity.CredentialValidator;
import ${groupId}.util.security.identity.TestCredentialValidator;
import ${groupId}.util.security.identity.CredentialValidatorChain;
import ${groupId}.util.security.identity.ELConfiguredLDAPCredentialValidator;

@ApplicationScoped
public class AppIdentityStore implements IdentityStore {

	private CredentialValidator credentialValidator = new CredentialValidatorChain(
			new TestCredentialValidator(),
			new ELConfiguredLDAPCredentialValidator());

	@Override
	public CredentialValidationResult validate(Credential credential) {
		return credentialValidator.validate(credential);
	}
}
