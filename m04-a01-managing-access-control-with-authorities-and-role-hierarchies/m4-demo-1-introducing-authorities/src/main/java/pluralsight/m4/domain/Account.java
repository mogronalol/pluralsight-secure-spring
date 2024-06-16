package pluralsight.m4.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.TreeSet;

@Data
@Builder
public final class Account {
    private final String username;
    private final String accountCode;
    private final int index;
    private final String displayName;
    @Builder.Default private final TreeSet<Transaction> transactions = new TreeSet<>();

    public BigDecimal getBalance() {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
