package pluralsight.m14.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
public final class Account {
    private final String username;
    private final String accountCode;
    private final int index;
    private final String displayName;
    private final String type;
    private final BigDecimal balance;

    public BigDecimal getBalance() {
        return balance.setScale(2, RoundingMode.HALF_UP);
    }
}
