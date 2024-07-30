package pluralsight.m10.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import pluralsight.m10.domain.Account;
import pluralsight.m10.domain.Transaction;
import pluralsight.m10.model.TransferModel;
import pluralsight.m10.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountsService {

    private final AccountRepository accountRepository;

    @PreAuthorize("@permissionAuthorizer.canPerformTransfer(#transfer)")
    public void transfer(final TransferModel transfer) {
        final Account from = accountRepository.findById(transfer.getFromAccountCode());
        from.getTransactions().add(Transaction.builder()
                .description("Transfer to " + transfer.getToAccountCode())
                .id(from.getTransactions().size())
                .date(LocalDateTime.now())
                .amount(transfer.getAmount().negate())
                .build());

        final Account to = accountRepository.findById(transfer.getToAccountCode());
        to.getTransactions().add(Transaction.builder()
                .description("Transfer from " + transfer.getToAccountCode())
                .id(from.getTransactions().size())
                .date(LocalDateTime.now())
                .amount(transfer.getAmount())
                .build());
    }

    @PostFilter("@permissionAuthorizer.getCanViewAccount(filterObject)")
    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    @PostAuthorize("@permissionAuthorizer.getCanViewAccount(returnObject)")
    public Account getAccountByCode(final String accountCode) {
        return accountRepository.findById(accountCode);
    }
}
