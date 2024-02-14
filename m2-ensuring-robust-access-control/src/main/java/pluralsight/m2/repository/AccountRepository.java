package pluralsight.m2.repository;

import org.springframework.stereotype.Component;
import pluralsight.m2.domain.Account;
import pluralsight.m2.domain.AccountType;
import pluralsight.m2.domain.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Component
public class AccountRepository {
    public static final String[] POSSIBLE_DESCRIPTIONS = {
            "Grocery Store Purchase",
            "Online Shopping",
            "Utility Bill Payment",
            "ATM Cash Withdrawal",
            "Monthly Rent",
            "Restaurant Dining",
            "Gas Station",
            "Subscription Service",
            "Gym Membership",
            "Insurance Premium"
    };

    public static final String[] USERNAMES = {
            "tom",
            "jane"
    };
    public static final Random RANDOM = new Random(1);

    private final List<Account> ACCOUNTS = generateAccounts();
    private final Map<String, List<Account>> ACCOUNTS_BY_USER = cacheAccountsByUser(ACCOUNTS);

    private static String generateAccountCode() {
        StringBuilder accountCode = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int digit = RANDOM.nextInt(10);
            accountCode.append(digit);
        }

        return accountCode.toString();
    }

    private Map<String, List<Account>> cacheAccountsByUser(final List<Account> accounts) {
        return accounts.stream()
                .collect(groupingBy(Account::userName));
    }

    public List<Account> getAccountForUser(final String username) {
        return ACCOUNTS_BY_USER.get(username);
    }

    private List<Account> generateAccounts() {
        return Stream.of(USERNAMES)
                .flatMap(username -> {
                    final int numberOfAccounts = RANDOM.nextInt(6) + 2;

                    return IntStream.range(0, numberOfAccounts)
                            .mapToObj(accountIndex -> {
                                final AccountType[] accountTypes = AccountType.values();
                                int randomIndex = RANDOM.nextInt(accountTypes.length);

                                final List<Transaction> transactions = IntStream.range(0, 10).mapToObj(transactionIndex -> {
                                    final LocalDateTime transactionDate = LocalDateTime.now().minusDays(transactionIndex);
                                    final String description = POSSIBLE_DESCRIPTIONS[RANDOM.nextInt(POSSIBLE_DESCRIPTIONS.length)];
                                    final double amount = Math.round(RANDOM.nextDouble() * 1000.0) / 100.0; // amounts up to 1000.00
                                    return new Transaction(transactionIndex, transactionDate, description, -amount);
                                }).toList();

                                final double accountBalance = transactions.stream()
                                        .collect(summarizingDouble(Transaction::amount))
                                        .getSum();

                                return new Account(generateAccountCode(), accountIndex, username, accountTypes[randomIndex].getDisplayName() + " " + (accountIndex + 1), accountBalance, transactions);
                            });
                })
                .collect(toList());
    }
}
