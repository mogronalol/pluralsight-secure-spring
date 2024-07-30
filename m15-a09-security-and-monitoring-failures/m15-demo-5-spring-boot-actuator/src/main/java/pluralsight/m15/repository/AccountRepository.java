package pluralsight.m15.repository;

import org.springframework.stereotype.Component;
import pluralsight.m15.domain.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AccountRepository {

    private final List<Account> allAccounts = new ArrayList<>();

    public List<Account> getAccountForUser(final String username) {
        return allAccounts.stream().filter(a -> a.getUsername().equals(username)).toList();
    }

    public void save(final Account account) {
        allAccounts.add(account);
    }

    public void deleteAll() {
        allAccounts.clear();
    }

    public List<Account> findAllAccounts() {
        return allAccounts;
    }

    public Account getAccountByCode(final String accountCode) {
        return allAccounts
                .stream()
                .filter(a -> a.getAccountCode().equals(accountCode))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public void saveAll(final Set<Account> allAccounts) {
        this.allAccounts.addAll(allAccounts);
    }
}
