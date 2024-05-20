package pluralsight.m7.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import pluralsight.m7.domain.Account;
import pluralsight.m7.domain.Transaction;
import pluralsight.m7.repository.AccountRepository;
import pluralsight.m7.model.TransferModel;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional
public class AccountsService {
    private final AccountRepository accountRepository;

    public AccountsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void transfer(final TransferModel transfer) {
        final Account from = accountRepository.getAccountByAccountCode(transfer.getFromAccountCode());
        from.getTransactions().add(Transaction.builder()
                .description("Transfer to " + transfer.getToAccountCode())
                .date(LocalDateTime.now())
                .amount(transfer.getAmount().negate())
                .build());

        final Account to = accountRepository.getAccountByAccountCode(transfer.getToAccountCode());
        to.getTransactions().add(Transaction.builder()
                .description("Transfer from " + transfer.getToAccountCode())
                .date(LocalDateTime.now())
                .amount(transfer.getAmount())
                .build());
    }

    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountByCode(final String accountCode) {
        return accountRepository.getAccountByAccountCode(accountCode);
    }
}
