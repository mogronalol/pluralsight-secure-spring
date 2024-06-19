package pluralsight.m12.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pluralsight.m12.domain.User;
import pluralsight.m12.domain.User.ResetToken;
import pluralsight.m12.domain.ValidationError;
import pluralsight.m12.repository.UserRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    public static final int MAX_LOGIN_BEFORE_LOCK = 3;
    private final UserRepository userRepository;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final EmailClient emailClient;

    private static Duration calculateNextLockDuration(final User user) {

        if (user.getFailedLoginAttempts() < MAX_LOGIN_BEFORE_LOCK) {
            return Duration.ZERO;
        }

        return Duration.ofMinutes(1);
    }

    public User getUserOrError(final String username) {
        return getUser(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public Optional<User> getUser(final String username) {
        return userRepository.getUser(username);
    }

    public boolean userExists(final String email) {
        return userRepository.getUser(email).isPresent();
    }

    public Set<ValidationError> createUser(final String email, final String password) {

        final Set<ValidationError> validationErrors = new HashSet<>();

        if (userExists(email)) {
            validationErrors.add(ValidationError.USER_ALREADY_EXISTS);
        }

        if (!password.isBlank() &&
            compromisedPasswordChecker.check(password).isCompromised()) {
            validationErrors.add(ValidationError.PASSWORD_COMPROMISED);
        }

        if (validationErrors.isEmpty()) {
            userRepository.save(User.builder()
                    .username(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .build());
        }

        return validationErrors;
    }

    public Set<ValidationError> updatePassword(final String email,
                                               final String resetToken,
                                               final String currentPassword,
                                               final String nextPassword) {

        final Set<ValidationError> validationErrors = new HashSet<>();

        final Optional<User> user = getUser(email);

        if (user.flatMap(User::getPasswordResetToken)
                .map(t -> mismatchedResetTokenOrExpiry(resetToken, t))
                .orElse(true)) {

            validationErrors.add(ValidationError.WRONG_OR_EXPIRED_TOKEN);
            return validationErrors;
        }

        user.ifPresent(u -> {
            if (!passwordEncoder.matches(currentPassword,
                    getUserOrError(email).getPasswordHash())) {
                validationErrors.add(ValidationError.WRONG_PASSWORD);
            }

            if (!nextPassword.isBlank() &&
                compromisedPasswordChecker.check(nextPassword).isCompromised()) {
                validationErrors.add(ValidationError.PASSWORD_COMPROMISED);
            }

            if (validationErrors.isEmpty()) {
                u.setPasswordHash(nextPassword);
                u.setPasswordResetToken(null);
                userRepository.save(u);
            }
        });

        return validationErrors;
    }

    private boolean mismatchedResetTokenOrExpiry(final String resetToken, final ResetToken t) {
        return !passwordEncoder.matches(resetToken, t.getResetTokenHash()) ||
               t.getResetTokenGeneratedAt().plusHours(1).isBefore(LocalDateTime.now(clock));
    }

    public void recordFailedLoginAttemptIfExists(String username) {
        getUser(username).ifPresent(u -> {
            u.setFailedLoginAttempts(u.getFailedLoginAttempts() + 1);
            u.setLastFailedLoginTime(LocalDateTime.now(clock));
            userRepository.save(u);
        });
    }

    public void assertUserNotLocked(User user) {
        user.getLastFailedLoginTime()
                .ifPresent(lastFailedLoginTime -> {

                    final Duration lockDuration = calculateNextLockDuration(user);
                    final LocalDateTime lockExpiry = lastFailedLoginTime.plus(lockDuration);
                    final LocalDateTime now = LocalDateTime.now(clock);

                    if (lockExpiry.isAfter(now)) {
                        throw new AccountLockedException(lockDuration, user.getUsername());
                    }
                });
    }

    public void resetFailedAttempts(String username) {
        User user = getUserOrError(username);
        user.setFailedLoginAttempts(0);
        user.setLastFailedLoginTime(null);
        userRepository.save(user);
    }

    public void triggerPasswordReset(String email) {
        userRepository.getUser(email)
                .ifPresent(u -> {
                    final String token = KeyGenerators.string().generateKey();
                    final String tokenHash = passwordEncoder.encode(token);
                    final ResetToken resetToken = new ResetToken();
                    resetToken.setResetTokenGeneratedAt(LocalDateTime.now(clock));
                    resetToken.setResetTokenHash(tokenHash);
                    u.setPasswordResetToken(
                            new ResetToken(tokenHash, LocalDateTime.now(clock)));
                    userRepository.save(u);
                    emailClient.sendPasswordResetEmail(email, token);
                });
    }
}
