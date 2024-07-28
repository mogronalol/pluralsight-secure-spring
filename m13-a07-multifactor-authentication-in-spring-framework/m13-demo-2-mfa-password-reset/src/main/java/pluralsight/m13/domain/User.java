package pluralsight.m13.domain;

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
    private SecureToken otpLoginToken;
    private SecureToken passwordResetToken;

    public Optional<LocalDateTime> getLastFailedLoginTime() {
        return Optional.ofNullable(lastFailedLoginTime);
    }

    public Optional<SecureToken> getOtpLoginToken() {
        return Optional.ofNullable(otpLoginToken);
    }

    public Optional<SecureToken> getPasswordResetToken() {
        return Optional.ofNullable(passwordResetToken);
    }

    @Data
    @Builder
    public static class SecureToken {
        private String tokenHash;
        private LocalDateTime generatedAt;
    }
}
