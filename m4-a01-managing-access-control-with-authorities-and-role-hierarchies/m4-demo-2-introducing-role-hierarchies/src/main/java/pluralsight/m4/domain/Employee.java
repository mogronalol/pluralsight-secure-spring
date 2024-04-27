package pluralsight.m4.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class Employee {

    private UUID employeeId;

    private String name;

    private String department;
}
