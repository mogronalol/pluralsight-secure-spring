package pluralsight.m8.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pluralsight.m8.domain.Account;
import pluralsight.m8.repository.AccountRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SqlInjectionProtectionTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void cannotInjectUnionQuery() throws Exception {
        final Account account = accountRepository.getAccountByAccountCode(
                """
                invalid' UNION SELECT id, 1, name, secret,
                'placeholder' FROM secret limit 1 --'
                """);

        assertThat(account).isNull();
    }
}
