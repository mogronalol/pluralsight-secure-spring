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
}
