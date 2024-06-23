package pluralsight.m14.repository;

import jakarta.annotation.PostConstruct;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pluralsight.m14.domain.Account;
import pluralsight.m14.domain.Transaction;
import pluralsight.m14.domain.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

@Component
public class TestDataFactory {
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

    public static final List<UserDetails> USERS = List.of(
            createUser()
    );

    public static final Random RANDOM = new Random(1);

    private final AccountRepository accountRepository;

    public TestDataFactory(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private static UserDetails createUser() {
        // Note: User.withDefaultPasswordEncoder() is deprecated and should only be used for
        // demonstration purposes
        return User.withDefaultPasswordEncoder()
                .username("jack")
                .password("password")
                .build();
    }

    public static Account generateAccount(final String username, final int accountIndex) {
        final AccountType[] accountTypes = AccountType.values();
        int randomIndex = RANDOM.nextInt(accountTypes.length);

        final TreeSet<Transaction> transactions =
                IntStream.range(0, 10).mapToObj(transactionIndex -> {
                    final LocalDateTime transactionDate =
                            LocalDateTime.now().minusDays(transactionIndex);
                    final String description = POSSIBLE_DESCRIPTIONS[RANDOM.nextInt(
                            POSSIBLE_DESCRIPTIONS.length)];
                    final BigDecimal amount = BigDecimal.valueOf(
                            Math.round(RANDOM.nextDouble() * 1000.0) /
                                    100.0); // amounts up to 1000.00

                    return Transaction.builder()
                            .date(transactionDate)
                            .amount(amount.negate())
                            .description(description)
                            .id(transactionIndex)
                            .build();
                }).collect(toCollection(TreeSet::new));

        return Account.builder()
                .accountCode(generateAccountCode())
                .index(accountIndex)
                .username(username)
                .displayName(
                        accountTypes[randomIndex].getDisplayName() + " " + (accountIndex + 1))
                .transactions(transactions)
                .build();
    }

    private static String generateAccountCode() {
        StringBuilder accountCode = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int digit = RANDOM.nextInt(10);
            accountCode.append(digit);
        }

        return accountCode.toString();
    }

    @PostConstruct
    public void generateAccounts() {
        saveAccount("tom");
        saveAccount("jane");
    }

    private void saveAccount(final String username) {
        final int numberOfAccounts = RANDOM.nextInt(6) + 2;

        IntStream.range(0, numberOfAccounts)
                .mapToObj(i -> generateAccount(username, i))
                .forEach(accountRepository::save);
    }
}
