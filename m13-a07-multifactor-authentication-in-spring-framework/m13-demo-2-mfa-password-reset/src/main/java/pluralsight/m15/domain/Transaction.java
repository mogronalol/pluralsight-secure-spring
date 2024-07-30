package pluralsight.m15.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public final class Transaction implements Comparable<Transaction> {
    private final long id;
    private final LocalDateTime date;
    private final String description;
    private final BigDecimal amount;

    @Override
    public int compareTo(final Transaction o) {
        return o.date.compareTo(this.date);
    }
}

