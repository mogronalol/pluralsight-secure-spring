package pluralsight.m6.controller.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pluralsight.m6.domain.Account;
import pluralsight.m6.repository.AccountRepository;

@RestController
public class AccountsController {
    private final AccountRepository accountRepository;

    public AccountsController(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/accounts/{accountCode}")
    public Account transactions(@PathVariable("accountCode") String accountCode) {
        return accountRepository.getAccountByCode(accountCode);
    }
}
