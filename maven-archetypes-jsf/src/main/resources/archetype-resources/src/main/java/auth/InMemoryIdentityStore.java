package ${groupId}.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import java.util.Arrays;
import java.util.HashSet;

@ApplicationScoped
public class InMemoryIdentityStore implements IdentityStore {

	@Override
	public CredentialValidationResult validate(Credential credential) {

		UsernamePasswordCredential login = (UsernamePasswordCredential) credential;

		if (login.getCaller().equals("admin") && login.getPasswordAsString().equals("password")) {
			return new CredentialValidationResult("admin", new HashSet<>(Arrays.asList("ADMIN", "USER")));
		} else if (login.getCaller().equals("alice") && login.getPasswordAsString().equals("password")) {
			return new CredentialValidationResult("alice", new HashSet<>(Arrays.asList("USER")));
		} else {
			return CredentialValidationResult.NOT_VALIDATED_RESULT;
		}
	}
}
