package pluralsight.m2.service;

import org.springframework.stereotype.Component;
import pluralsight.m2.domain.Account;
import pluralsight.m2.domain.Transaction;
import pluralsight.m2.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class AccountsService {
    private final AccountRepository accountRepository;

    public AccountsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void transfer(final String fromAccountCode, final String toAccountCode, final BigDecimal amount){
        final Account from = accountRepository.getAccountByCode(fromAccountCode);
        from.getTransactions().add(Transaction.builder()
                        .description("Transfer to " + toAccountCode)
                        .id(from.getTransactions().size())
                        .date(LocalDateTime.now())
                        .amount(amount.negate())
                .build());

        final Account to = accountRepository.getAccountByCode(toAccountCode);
        to.getTransactions().add(Transaction.builder()
                .description("Transfer from " + toAccountCode)
                .id(from.getTransactions().size())
                .date(LocalDateTime.now())
                .amount(amount)
                .build());
    }

    public List<Account> findAllAccounts() {
        return accountRepository.findAllAccounts();
    }

    public Account getAccountByCode(final String accountCode) {
        return accountRepository.getAccountByCode(accountCode);
    }
}
