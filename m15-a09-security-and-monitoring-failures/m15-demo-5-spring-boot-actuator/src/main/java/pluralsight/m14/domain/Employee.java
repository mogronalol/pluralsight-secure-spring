package pluralsight.m14.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pluralsight.m14.security.Unmasked;
import pluralsight.m14.security.MaskedToString;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
@Builder
public class Employee extends MaskedToString {
    private UUID employeeId;

    private String name;

    @Unmasked
    private String department;
}
