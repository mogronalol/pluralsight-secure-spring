package pluralsight.m15.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import pluralsight.m15.domain.Account;
import pluralsight.m15.domain.Transaction;
import pluralsight.m15.model.TransferModel;
import pluralsight.m15.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountsService {

    private final AccountRepository accountRepository;

    @PreAuthorize("@permissionAuthorizer.canPerformTransfer(#transfer)")
    public void transfer(final TransferModel transfer) {

        log.info("Performing transfer... {}", transfer);

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

    @PostFilter("@permissionAuthorizer.getCanViewAccount(filterObject)")
    public List<Account> findAllAccounts() {
        log.info("Finding all accounts...");
        return accountRepository.findAllAccounts();
    }

    @PostAuthorize("@permissionAuthorizer.getCanViewAccount(returnObject)")
    public Account getAccountByCode(final String accountCode) {
        log.info("Getting account by code... {}", accountCode);
        return accountRepository.getAccountByCode(accountCode);
    }
}
