package pluralsight.m12.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@Builder
public class User {
    private String username;
    private String passwordHash;
    private int failedLoginAttempts;
    private LocalDateTime lastFailedLoginTime;

    public Optional<LocalDateTime> getLastFailedLoginTime() {
        return Optional.ofNullable(lastFailedLoginTime);
    }
}
