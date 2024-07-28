package pluralsight.m12.controller.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pluralsight.m12.domain.User;
import pluralsight.m12.repository.UserRepository;
import pluralsight.m12.service.AccountLockedException;
import pluralsight.m12.service.UserService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private UserRepository userRepository;
    private Clock clock;

    @BeforeEach
    public void setUp() {
        userRepository = new UserRepository();
        clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
        userService = new UserService(userRepository,
                null,
                null,
                clock);
    }

    @Test
    public void testRecordFailedLoginAttempt() {
        User user = User.builder()
                .username("username")
                .failedLoginAttempts(1)
                .lastFailedLoginTime(null)
                .build();
        userRepository.saveUser(user);

        userService.recordFailedLoginAttemptIfExists("username");

        final User updatedUser = userRepository.getUser("username").orElseThrow();

        assertThat(updatedUser.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(updatedUser.getLastFailedLoginTime()).contains(LocalDateTime.now(clock));
    }

    @Test
    public void shouldResetFailedLoginAttempt() {
        User user = User.builder()
                .username("username")
                .failedLoginAttempts(3)
                .lastFailedLoginTime(LocalDateTime.now())
                .build();
        userRepository.saveUser(user);

        userService.resetFailedLoginAttempts("username");

        final User updatedUser = userRepository.getUser("username").orElseThrow();

        assertThat(updatedUser.getFailedLoginAttempts()).isZero();
        assertThat(updatedUser.getLastFailedLoginTime()).isEmpty();
    }

    @Test
    public void assertLockedAfter3AttemptsLessThanOneMinuteAgo() {
        User user = User.builder()
                .failedLoginAttempts(3)
                .lastFailedLoginTime(LocalDateTime.now(clock).minusMinutes(1).plusSeconds(1))
                .build();
        userRepository.saveUser(user);

        assertThatThrownBy(() -> userService.assertUserNotLocked(user))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    public void assertNotLockedAfter3AttemptsExactlyOneMinuteAgo() {
        User user = User.builder()
                .failedLoginAttempts(3)
                .lastFailedLoginTime(LocalDateTime.now(clock).minusMinutes(1))
                .build();
        userRepository.saveUser(user);

        assertThatCode(() -> userService.assertUserNotLocked(user)).doesNotThrowAnyException();
    }

    @Test
    public void assertNotLockedAfter2AttemptsLessThanOneMinuteAgo() {
        User user = User.builder()
                .failedLoginAttempts(2)
                .lastFailedLoginTime(LocalDateTime.now(clock).minusMinutes(1).plusSeconds(1))
                .build();
        userRepository.saveUser(user);

        assertThatCode(() -> userService.assertUserNotLocked(user)).doesNotThrowAnyException();
    }

    @Test
    public void assertNotLockedWhenNoLastFailedLoginTime() {
        User user = User.builder()
                .failedLoginAttempts(3)
                .lastFailedLoginTime(null)
                .build();
        userRepository.saveUser(user);

        assertThatCode(() -> userService.assertUserNotLocked(user)).doesNotThrowAnyException();
    }
}
