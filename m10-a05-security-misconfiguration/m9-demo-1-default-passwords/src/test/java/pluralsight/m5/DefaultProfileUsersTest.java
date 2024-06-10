package pluralsight.m5;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import pluralsight.m5.repository.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
public class DefaultProfileUsersTest {
    @Autowired
    private UserDetailsManager userDetailsManager;

    @Test
    public void noTestUsers() {
        TestDataFactory.USERS
                .forEach(u -> assertThatThrownBy(() -> userDetailsManager.loadUserByUsername(u.getUsername()))
                        .isInstanceOf(UsernameNotFoundException.class));
    }
}

