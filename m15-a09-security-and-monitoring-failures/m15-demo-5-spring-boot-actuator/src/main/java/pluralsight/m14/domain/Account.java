package pluralsight.m14.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pluralsight.m14.security.Unmasked;
import pluralsight.m14.security.MaskedToString;

import java.math.BigDecimal;
import java.util.TreeSet;

@EqualsAndHashCode(callSuper = true)
@Getter
@RequiredArgsConstructor
@Builder
public final class Account extends MaskedToString {
    private final String username;
    private final String accountCode;
    @Unmasked
    private final int index;
    private final String displayName;
    @Unmasked
    @Builder.Default private final TreeSet<Transaction> transactions = new TreeSet<>();

    public BigDecimal getBalance() {
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
