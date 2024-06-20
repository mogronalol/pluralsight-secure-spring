package pluralsight.m12.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String username;
    private UUID userId;
    private String passwordHash;
    private SecureToken passwordResetToken;
    private int failedLoginAttempts;
    private LocalDateTime lastFailedLoginTime;

    public Optional<LocalDateTime> getLastFailedLoginTime() {
        return Optional.ofNullable(lastFailedLoginTime);
    }

    public Optional<SecureToken> getPasswordResetToken() {
        return Optional.ofNullable(passwordResetToken);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SecureToken {
        private String resetTokenHash;
        private LocalDateTime resetTokenGeneratedAt;
    }
}
