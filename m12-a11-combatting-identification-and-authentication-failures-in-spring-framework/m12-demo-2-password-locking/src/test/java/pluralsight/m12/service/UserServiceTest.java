package pluralsight.m12.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pluralsight.m12.domain.User;
import pluralsight.m12.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository; // Not mocked, assumed to be interacting directly
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository(); // This would need to be an actual class that interacts with the database.
        fixedClock = Clock.fixed(Instant.parse("2020-04-08T10:15:30.00Z"), ZoneId.systemDefault());
        userService = new UserService(userRepository, null, null, fixedClock); // Assuming compromisedPasswordChecker and passwordEncoder are not used in the tests
    }

    @Test
    void shouldResetFailedLoginAttempt() {
        String username = "user@example.com";
        User user = User.builder()
                .username(username)
                .failedLoginAttempts(3)
                .lastFailedLoginTime(LocalDateTime.now())
                .build();
        userRepository.saveUser(user);

        userService.resetFailedAttempts(username);

        User updatedUser = userRepository.getUser(username).orElseThrow();
        assertThat(updatedUser.getFailedLoginAttempts()).isZero();
        assertThat(updatedUser.getLastFailedLoginTime()).isNull();
    }

    @Test
    void testRecordFailedLoginAttempt() {
        String username = "user@example.com";
        User user = User.builder()
                .username(username)
                .failedLoginAttempts(1)
                .build();
        userRepository.saveUser(user);

        userService.recordFailedLoginAttemptIfExists(username);

        User updatedUser = userRepository.getUser(username).orElseThrow();
        assertThat(updatedUser.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(updatedUser.getLastFailedLoginTime()).isEqualTo(LocalDateTime.now(fixedClock));
    }

    @Test
    void testAssertUserNotLocked_NotLocked() {
        User user = User.builder()
                .failedLoginAttempts(4)
                .lastFailedLoginTime(LocalDateTime.now(fixedClock).minusMinutes(2))
                .build();
        userRepository.saveUser(user);

        assertThatCode(() -> userService.assertUserNotLocked(user)).doesNotThrowAnyException();
    }

    @Test
    void testAssertUserNotLocked_IsLocked() {
        User user = User.builder()
                .failedLoginAttempts(4) // More than MAX_LOGIN_BEFORE_LOCK
                .lastFailedLoginTime(LocalDateTime.now(fixedClock))
                .build();
        userRepository.saveUser(user);

        assertThatThrownBy(() -> userService.assertUserNotLocked(user))
                .isInstanceOf(RuntimeException.class) // Replace RuntimeException with your specific exception class
                .hasMessageContaining("locked"); // Optional, check for specific message content if necessary
    }
}