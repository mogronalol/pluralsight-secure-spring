package pluralsight.m12.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class User {
    private String username;
    private UUID userId;
    private String passwordHash;
}
