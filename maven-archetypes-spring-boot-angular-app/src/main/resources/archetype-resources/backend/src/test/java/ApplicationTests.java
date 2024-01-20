package ${groupId};

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTests {

	@Test
	public void contextLoads() {
	}

    @Autowired
    private MockMvc mockMvc;

	@Test
	void givenUserIsAuthenticated_whenGetMe_thenOk() throws Exception {

	    mockMvc
	    	.perform(get("/api/me")
	    	.with(jwt()
	    			.authorities(List.of(
	    					new SimpleGrantedAuthority("admin"),
	    					new SimpleGrantedAuthority("user")))
	    			.jwt(jwt -> {
							jwt.issuer("http://127.0.0.1/issuer");
							jwt.issuedAt(Instant.now());
							jwt.expiresAt(Instant.now().plusMillis(5000));
							jwt.audience(Set.of("app-client"));
							jwt.subject("alice");
							jwt.claim(StandardClaimNames.PREFERRED_USERNAME, "alice");
							jwt.claim("fullName", "Alice");
							jwt.claim("roles", List.of("admin", "user"));
		    		})))
	    	.andDo(print())
	    	.andExpect(status().isOk())
	    	.andExpect(jsonPath("$.data.sub", is("alice")));
	}
}
