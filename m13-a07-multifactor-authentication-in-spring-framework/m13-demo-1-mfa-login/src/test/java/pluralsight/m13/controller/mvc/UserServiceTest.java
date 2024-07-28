package pluralsight.m13.controller.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pluralsight.m13.domain.User;
import pluralsight.m13.repository.UserRepository;
import pluralsight.m13.security.MfaClient;
import pluralsight.m13.service.AccountLockedException;
import pluralsight.m13.service.UserService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UserServiceTest {
    private UserService userService;
    private UserRepository userRepository;
    private Clock clock;
    private MfaClient mfaClient;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        userRepository = new UserRepository();
        clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
        mfaClient = mock(MfaClient.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository,
                passwordEncoder,
                null,
                clock,
                mfaClient);
    }

    @Test
    public void shouldSendOtp() {
        userRepository.saveUser(User.builder().username("username").build());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        userService.triggerOtp("username");

        verify(mfaClient).sendOtp(eq("username"), tokenCaptor.capture());

        final String tokenCaptorValue = tokenCaptor.getValue();

        assertThat(userService.getUserOrError("username").getOtpLoginToken())
                .isPresent()
                .hasValueSatisfying(userTokenHash -> {
                    assertThat(passwordEncoder.matches(tokenCaptorValue,
                            userTokenHash.getTokenHash())).isTrue();

                    assertThat(userTokenHash.getGeneratedAt())
                            .isEqualTo(LocalDateTime.now(clock));
                });
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

    @Test
    public void verifyWrongOtpTokenIsInvalid() {
        final User user = User.builder()
                .otpLoginToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .build();
        userRepository.saveUser(user);

        final boolean isValid = userService.verifyOtp("wrong", "username");

        assertThat(isValid).isFalse();
        assertThat(user.getOtpLoginToken()).isPresent();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getLastFailedLoginTime()).contains(LocalDateTime.now(clock));
    }

    @Test
    public void verifyExpiredOtpTokenIsInvalid() {
        final User user = User.builder()
                .otpLoginToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock).minusMinutes(10))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .build();
        userRepository.saveUser(user);

        final boolean isValid = userService.verifyOtp("token", "username");

        assertThat(isValid).isFalse();
        assertThat(user.getOtpLoginToken()).isPresent();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getLastFailedLoginTime()).contains(LocalDateTime.now(clock));
    }

    @Test
    public void accountShouldBeLockedAfterThreeFailedLoginAttempts() {
        final User user = User.builder()
                .otpLoginToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock).minusMinutes(10))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .build();
        userRepository.saveUser(user);

        assertThatThrownBy(() -> {
            for (int i = 0; i < 3; i++) {
                userService.verifyOtp("token", "username");
            }
        }).isInstanceOf(AccountLockedException.class);

    }

    @Test
    public void verifyUnexpiredMatchingOtpTokenIsValid() {
        final User user = User.builder()
                .otpLoginToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock).minusMinutes(10).plusSeconds(1))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .lastFailedLoginTime(LocalDateTime.now(clock))
                .failedLoginAttempts(2)
                .build();
        userRepository.saveUser(user);

        final boolean isValid = userService.verifyOtp("token", "username");

        assertThat(isValid).isTrue();
        assertThat(user.getOtpLoginToken()).isEmpty();
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLastFailedLoginTime()).isEmpty();
    }
}
