package pluralsight.m14.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pluralsight.m14.security.Unmasked;
import pluralsight.m14.security.MaskedToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Getter
@RequiredArgsConstructor
@Builder
public final class Transaction extends MaskedToString implements Comparable<Transaction> {
    @Unmasked
    private final long id;
    private final LocalDateTime date;
    private final String description;
    private final BigDecimal amount;

    @Override
    public int compareTo(final Transaction o) {
        return o.date.compareTo(this.date);
    }
}

