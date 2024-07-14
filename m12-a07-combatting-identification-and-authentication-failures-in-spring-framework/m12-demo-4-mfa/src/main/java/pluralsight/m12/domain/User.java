package pluralsight.m12.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pluralsight.m12.security.Roles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class User {
    private String username;
    private UUID userId;
    private String passwordHash;
    private SecureToken otpLoginToken;
    private int failedLoginAttempts;
    private LocalDateTime lastFailedLoginTime;
    private Set<Roles> roles = new HashSet<>();

    public Optional<LocalDateTime> getLastFailedLoginTime() {
        return Optional.ofNullable(lastFailedLoginTime);
    }


    public Optional<SecureToken> getOtpLoginToken() {
        return Optional.ofNullable(otpLoginToken);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SecureToken {
        private String tokenHash;
        private LocalDateTime generatedAt;
    }
}
