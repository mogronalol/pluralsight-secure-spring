package pluralsight.m2.service;

import org.springframework.stereotype.Component;
import pluralsight.m2.domain.Account;
import pluralsight.m2.domain.Transaction;
import pluralsight.m2.model.TransferModel;
import pluralsight.m2.repository.AccountRepository;
import pluralsight.m2.repository.FraudRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AccountsService {
    private final AccountRepository accountRepository;
    private final FraudRepository fraudRepository;

    public AccountsService(AccountRepository accountRepository, FraudRepository fraudRepository) {
        this.accountRepository = accountRepository;
        this.fraudRepository = fraudRepository;
    }

    public void transfer(final TransferModel transfer) {
        final Account from = accountRepository.getAccountByCode(transfer.getFromAccountCode());
        from.getTransactions().add(Transaction.builder()
                .description("Transfer to " + transfer.getToAccountCode())
                .id(from.getTransactions().size())
                .date(LocalDateTime.now())
                .amount(transfer.getAmount().negate())
                .build());

        final Account to = accountRepository.getAccountByCode(transfer.getToAccountCode());
        to.getTransactions().add(Transaction.builder()
                .description("Transfer from " + transfer.getToAccountCode())
                .id(from.getTransactions().size())
                .date(LocalDateTime.now())
                .amount(transfer.getAmount())
                .build());
    }

    public List<Account> findAllAccounts() {
        return accountRepository.findAllAccounts();
    }

    public Account getAccountByCode(final String accountCode) {
        return accountRepository.getAccountByCode(accountCode);
    }

    public boolean isFraudSuspectedAccount(final String accountCode) {
        return fraudRepository.isFraudSuspected(accountCode);
    }
}
