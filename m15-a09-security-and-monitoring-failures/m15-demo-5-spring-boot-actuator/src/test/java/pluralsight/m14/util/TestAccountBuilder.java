package pluralsight.m14.util;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import pluralsight.m14.domain.Account;
import pluralsight.m14.domain.Transaction;

import java.util.TreeSet;

@Data
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestAccountBuilder {
    private final TreeSet<Transaction> transactions = new TreeSet<>();
    private String username = "user";
    private String accountCode = "code";
    private int index = 1;
    private String displayName = "account";

    public static TestAccountBuilder testAccountBuilder() {
        return new TestAccountBuilder();
    }

    public Account build() {
        return Account.builder()
                .username(username)
                .displayName(displayName)
                .index(index)
                .accountCode(accountCode)
                .transactions(transactions)
                .build();
    }
}
