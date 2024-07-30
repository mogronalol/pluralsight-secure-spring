package pluralsight.m10;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pluralsight.m10.repository.AccountRepository;
import pluralsight.m10.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local-development")
public class LocalDevelopmentProfileConfigurationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void shouldCreateTestUsersAndAccounts() {
        assertThat(userRepository.findById("test1"))
                .isNotNull();

        Assertions.assertThat(accountRepository.findById("1000000"))
                .isNotNull();
    }
}
