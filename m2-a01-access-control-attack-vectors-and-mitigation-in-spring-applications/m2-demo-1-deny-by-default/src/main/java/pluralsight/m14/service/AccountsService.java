package pluralsight.m14.service;

import org.springframework.stereotype.Component;
import pluralsight.m14.domain.Account;
import pluralsight.m14.domain.Transaction;
import pluralsight.m14.repository.AccountRepository;
import pluralsight.m14.model.TransferModel;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AccountsService {
    private final AccountRepository accountRepository;

    public AccountsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
}