package pluralsight.m12.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String username;
    private String passwordHash;
}
