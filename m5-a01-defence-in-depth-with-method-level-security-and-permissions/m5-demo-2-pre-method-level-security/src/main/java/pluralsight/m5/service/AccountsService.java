package pluralsight.m5.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import pluralsight.m5.domain.Account;
import pluralsight.m5.domain.Transaction;
import pluralsight.m5.model.TransferModel;
import pluralsight.m5.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountsService {

    private final AccountRepository accountRepository;

    @PreAuthorize("""
hasAuthority('LARGE_TRANSFERS')
|| #transfer.amount.compareTo(new java.math.BigDecimal('500')) < 0 && hasAuthority('TRANSFERS')
""")
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

    @PostFilter("""
            filterObject.username == authentication.name || hasAuthority('VIEW_ACCOUNTS')
            """)
    public List<Account> findAllAccounts() {
        return accountRepository.findAllAccounts();
    }

    @PostAuthorize("""
            returnObject.username == authentication.name || hasAuthority('VIEW_ACCOUNTS')
            """)
    public Account getAccountByCode(final String accountCode) {
        return accountRepository.getAccountByCode(accountCode);
    }
}
