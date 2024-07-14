package pluralsight.m12.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pluralsight.m12.domain.User;
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
            userRepository.saveUser(User.builder()
                    .username(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .build());
        }

        return validationErrors;
    }

    public Set<ValidationError> updatePassword(final String email,
                                               final String currentPassword,
                                               final String nextPassword) {

        final Set<ValidationError> validationErrors = new HashSet<>();

        if (userExists(email)) {
            if (!passwordEncoder.matches(currentPassword,
                    getUserOrError(email).getPasswordHash())) {
                validationErrors.add(ValidationError.WRONG_PASSWORD);
            }
        } else {
            validationErrors.add(ValidationError.USER_DOES_NOT_EXIST);
        }

        if (!nextPassword.isBlank() &&
            compromisedPasswordChecker.check(nextPassword).isCompromised()) {
            validationErrors.add(ValidationError.PASSWORD_COMPROMISED);
        }

        if (validationErrors.isEmpty()) {
            userRepository.saveUser(User.builder()
                    .username(email)
                    .passwordHash(nextPassword)
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
        userRepository.saveUser(user);
    }
}
