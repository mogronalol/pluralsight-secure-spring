package pluralsight.m6.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public final class Account {
    private final String username;
    private final String accountCode;
    private final int index;
    private final String displayName;
}
