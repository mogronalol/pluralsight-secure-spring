package pluralsight.m5;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.context.ActiveProfiles;
import pluralsight.m5.repository.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local-development")
public class LocalDevelopmentProfileUsersTest {
    @Autowired
    private UserDetailsManager userDetailsManager;

    @Test
    public void cannotLoginWithDefaultUser() {
        TestDataFactory.USERS
                .forEach(u -> assertThat(userDetailsManager.loadUserByUsername(u.getUsername())).isNotNull());
    }
}

