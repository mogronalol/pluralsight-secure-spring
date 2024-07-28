package pluralsight.m13.domain;

import lombok.Builder;
import lombok.Getter;
import pluralsight.m13.security.MaskedToString;

import java.util.UUID;

@Getter
@Builder
public class Employee extends MaskedToString {
    private UUID employeeId;

    private String name;

    private String department;
}
