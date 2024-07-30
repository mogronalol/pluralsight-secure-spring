package pluralsight.m15.domain;

import lombok.Builder;
import lombok.Getter;
import pluralsight.m15.security.MaskedToString;
import pluralsight.m15.security.Unmasked;

import java.util.UUID;

@Getter
@Builder
public class Employee extends MaskedToString {
    private UUID employeeId;

    private String name;

    @Unmasked
    private String department;
}
