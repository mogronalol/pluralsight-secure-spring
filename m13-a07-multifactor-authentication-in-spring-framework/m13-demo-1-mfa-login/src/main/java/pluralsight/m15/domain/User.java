package pluralsight.m15.domain;

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

    public Optional<LocalDateTime> getLastFailedLoginTime() {
        return Optional.ofNullable(lastFailedLoginTime);
    }

    public Optional<SecureToken> getOtpLoginToken() {
        return Optional.ofNullable(otpLoginToken);
    }

    @Data
    @Builder
    public static class SecureToken {
        private String tokenHash;
        private LocalDateTime generatedAt;
    }
}
