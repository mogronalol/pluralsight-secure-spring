package pluralsight.m15.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pluralsight.m15.domain.Account;
import pluralsight.m15.repository.AccountRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountsService {

    private final AccountRepository accountRepository;

    public List<Account> findAllAccounts() {
        return accountRepository.findAllAccounts();
    }

    public Account getAccountByCode(final String accountCode) {
        return accountRepository.getAccountByCode(accountCode);
    }
}
