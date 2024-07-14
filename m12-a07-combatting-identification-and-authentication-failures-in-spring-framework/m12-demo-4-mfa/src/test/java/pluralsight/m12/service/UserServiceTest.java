package pluralsight.m12.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import pluralsight.m12.domain.User;
import pluralsight.m12.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private Clock fixedClock;
    private DelegatingPasswordEncoder passwordEncoder;
    private MfaClient mfaClient;

    @BeforeEach
    void setUp() {
        mfaClient = mock(MfaClient.class);
        userRepository = new UserRepository();
        fixedClock = Clock.fixed(Instant.parse("2020-04-08T10:15:30.00Z"), ZoneId.systemDefault());
        userService = new UserService(userRepository,
                null,
                passwordEncoder,
                fixedClock,
                mfaClient);
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
        assertThat(updatedUser.getLastFailedLoginTime()).isEmpty();
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
        assertThat(updatedUser.getLastFailedLoginTime()).contains(LocalDateTime.now(fixedClock));
    }

    @Test
    void assertLockedAfter3AttemptsMoreThan1MinuteAgo() {
        User user = User.builder()
                .failedLoginAttempts(3)
                .lastFailedLoginTime(LocalDateTime.now(fixedClock).minusMinutes(1).plusSeconds(1))
                .build();
        userRepository.saveUser(user);

        assertThatThrownBy(() -> userService.assertUserNotLocked(user))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void assertNotLockedAfter2AttemptsLessThan1MinuteAgo() {
        User user = User.builder()
                .failedLoginAttempts(2)
                .lastFailedLoginTime(LocalDateTime.now(fixedClock).minusMinutes(1).plusSeconds(1))
                .build();
        userRepository.saveUser(user);

        assertThatCode(() -> userService.assertUserNotLocked(user)).doesNotThrowAnyException();
    }

    @Test
    void assertNotLockedAfter3AttemptsMore1MinuteAgo() {
        User user = User.builder()
                .failedLoginAttempts(3)
                .lastFailedLoginTime(LocalDateTime.now(fixedClock).minusMinutes(1).minusSeconds(1))
                .build();
        userRepository.saveUser(user);

        assertThatCode(() -> userService.assertUserNotLocked(user)).doesNotThrowAnyException();
    }

    @Test
    void verifyOtpForNonExistentUser() {
        final boolean valid = userService.verifyOtp("wrongUser", "wrongToken");
        assertThat(valid).isFalse();
    }

    @Test
    void verifyWrongOtp() {
        String email = "user@example.com";
        String storedToken = "storedToken";
        final User.SecureToken resetToken = new User.SecureToken(passwordEncoder.encode(storedToken), LocalDateTime.now());
        userRepository.saveUser(User.builder()
                .username(email)
                .passwordHash(passwordEncoder.encode("currentPassword"))
                .otpLoginToken(resetToken)
                .failedLoginAttempts(0)
                .build());

        final boolean valid = userService.verifyOtp("wrongToken", email);

        assertThat(valid).isFalse();
        assertThat(userRepository.getUser(email))
                .hasValueSatisfying(u -> {
                    assertThat(u.getOtpLoginToken()).contains(resetToken);
                    assertThat(u.getFailedLoginAttempts()).isEqualTo(1);
                    assertThat(u.getLastFailedLoginTime()).contains(LocalDateTime.now(fixedClock));
                });
    }

    @Test
    void verifyOtpWithExpiredToken() {
        String email = "user@example.com";
        String validToken = "validToken";
        final User.SecureToken resetToken = new User.SecureToken(passwordEncoder.encode(validToken), LocalDateTime.now().minusDays(1));
        userRepository.saveUser(User.builder()
                .username(email)
                .passwordHash(passwordEncoder.encode("currentPassword"))
                .otpLoginToken(resetToken)
                .build());

        final boolean valid = userService.verifyOtp(validToken, email);

        assertThat(valid).isFalse();
        assertThat(userRepository.getUser(email))
                .hasValueSatisfying(u -> {
                    assertThat(u.getOtpLoginToken()).contains(resetToken);
                    assertThat(u.getFailedLoginAttempts()).isEqualTo(1);
                    assertThat(u.getLastFailedLoginTime()).contains(LocalDateTime.now(fixedClock));
                });
    }

    @Test
    void verifyOtpSuccess() {
        String email = "user@example.com";
        String storedToken = "storedToken";
        final User.SecureToken resetToken = new User.SecureToken(passwordEncoder.encode(storedToken), LocalDateTime.now());
        userRepository.saveUser(User.builder()
                .username(email)
                .passwordHash(passwordEncoder.encode("currentPassword"))
                .otpLoginToken(resetToken)
                .failedLoginAttempts(2)
                .lastFailedLoginTime(LocalDateTime.now(fixedClock))
                .build());

        final boolean isValid = userService.verifyOtp(storedToken, email);

        assertThat(isValid).isTrue();
        assertThat(userRepository.getUser(email))
                .hasValueSatisfying(u -> {
                    assertThat(u.getOtpLoginToken()).isEmpty();
                    assertThat(u.getFailedLoginAttempts()).isEqualTo(0);
                    assertThat(u.getLastFailedLoginTime()).isEmpty();
                });
    }
}