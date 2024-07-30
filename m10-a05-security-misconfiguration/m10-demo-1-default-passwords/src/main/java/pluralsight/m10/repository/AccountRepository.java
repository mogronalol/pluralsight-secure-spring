package pluralsight.m10.repository;

import org.springframework.stereotype.Component;
import pluralsight.m10.domain.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class AccountRepository implements GenericRepository<Account, String> {

    private final List<Account> allAccounts = new ArrayList<>();

    @Override
    public void save(final Account account) {
        allAccounts.add(account);
    }

    public void saveAll(final Set<Account> accounts) {
        allAccounts.addAll(accounts);
    }

    @Override
    public void deleteAll() {
        allAccounts.clear();
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(allAccounts);
    }

    @Override
    public Account findById(final String accountCode) {
        return allAccounts.stream()
                          .filter(a -> a.getAccountCode().equals(accountCode))
                          .findFirst()
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public List<Account> getAccountForUser(final String username) {
        return allAccounts.stream().filter(a -> a.getUsername().equals(username)).toList();
    }
}
