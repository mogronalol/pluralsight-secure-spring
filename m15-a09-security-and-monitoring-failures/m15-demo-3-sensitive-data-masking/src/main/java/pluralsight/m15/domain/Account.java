package pluralsight.m15.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pluralsight.m15.security.MaskedToString;

import java.math.BigDecimal;
import java.util.TreeSet;

@Getter
@RequiredArgsConstructor
@Builder
public final class Account extends MaskedToString {
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
