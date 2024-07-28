package pluralsight.m13.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pluralsight.m13.domain.User;
import pluralsight.m13.domain.ValidationError;
import pluralsight.m13.repository.UserRepository;
import pluralsight.m13.security.MfaLoginClient;

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
    private final PasswordEncoder passwordEncoder;
    private final CompromisedPasswordChecker compromisedPasswordChecker;
    private final Clock clock;
    private final MfaLoginClient mfaClient;

    public void triggerOtp(String username) {
        final User user = getUserOrError(username);
        final String token = KeyGenerators.string().generateKey();
        final String encodedToken = passwordEncoder.encode(token);
        final User.SecureToken otp = User.SecureToken.builder()
                .tokenHash(encodedToken)
                .generatedAt(LocalDateTime.now(clock))
                .build();
        user.setOtpLoginToken(otp);
        userRepository.saveUser(user);
        mfaClient.sendOtp(username, token);
    }

    public boolean verifyOtp(String otp, String username) {
        final User user = getUserOrError(username);
        final Optional<User.SecureToken> otpLoginToken = user.getOtpLoginToken();
        final boolean validToken = otpLoginToken
                .filter(t -> passwordEncoder.matches(otp, t.getTokenHash()))
                .filter(t -> t.getGeneratedAt()
                        .isAfter(LocalDateTime.now(clock).minusMinutes(10)))
                .isPresent();

        if (validToken) {
            user.setOtpLoginToken(null);
            resetFailedLoginAttempts(username);
            userRepository.saveUser(user);
        } else {
            recordFailedLoginAttemptIfExists(username);
            assertUserNotLocked(user);
        }

        return validToken;
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

        validatePassword(password, validationErrors);

        if (validationErrors.isEmpty()) {
            userRepository.saveUser(User.builder()
                    .username(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .build());
        }

        return validationErrors;
    }

    public void recordFailedLoginAttemptIfExists(String username) {
        getUser(username).ifPresent(u -> {
            u.setFailedLoginAttempts(u.getFailedLoginAttempts() + 1);
            u.setLastFailedLoginTime(LocalDateTime.now(clock));
            userRepository.saveUser(u);
        });
    }

    public void resetFailedLoginAttempts(String username) {
        final User user = getUserOrError(username);
        user.setFailedLoginAttempts(0);
        user.setLastFailedLoginTime(null);
        userRepository.saveUser(user);
    }

    public void assertUserNotLocked(User user) {
        user.getLastFailedLoginTime()
                .ifPresent(lastFailedLoginTime -> {
                    final Duration lockDuration = calculateNextLockDuration(user);
                    final LocalDateTime lockExpiry = lastFailedLoginTime.plus(lockDuration);
                    final LocalDateTime now = LocalDateTime.now(clock);
                    if (lockExpiry.isAfter(now)) {
                        throw new AccountLockedException(user.getUsername());
                    }
                });
    }

    private static Duration calculateNextLockDuration(final User user) {
        if (user.getFailedLoginAttempts() < MAX_LOGIN_BEFORE_LOCK) {
            return Duration.ZERO;
        }
        return Duration.ofMinutes(1);
    }

    private void validatePassword(final String password,
                                  final Set<ValidationError> validationErrors) {

        if (!password.isBlank() &&
            compromisedPasswordChecker.check(password).isCompromised()) {
            validationErrors.add(ValidationError.COMPROMISED_PASSWORD);
        }
    }
}
