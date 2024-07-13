package pluralsight.m5;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pluralsight.m5.repository.AccountRepository;
import pluralsight.m5.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local-development")
public class LocalDevelopmentProfileConfigurationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void shouldCreateTestEmployeesAndAccounts() {
        assertThat(userRepository.findById("test1"))
                .isNotNull();

        assertThat(accountRepository.findById("1000000"))
                .isNotNull();
    }
}

