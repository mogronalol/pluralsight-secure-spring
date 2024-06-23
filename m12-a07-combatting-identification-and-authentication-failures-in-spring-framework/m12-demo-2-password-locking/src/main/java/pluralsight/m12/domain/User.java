package pluralsight.m12.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
public class User {
    private String username;
    private UUID userId;
    private String passwordHash;
    private int failedLoginAttempts;
    private LocalDateTime lastFailedLoginTime;

    public Optional<LocalDateTime> getLastFailedLoginTime() {
        return Optional.ofNullable(lastFailedLoginTime);
    }
}
