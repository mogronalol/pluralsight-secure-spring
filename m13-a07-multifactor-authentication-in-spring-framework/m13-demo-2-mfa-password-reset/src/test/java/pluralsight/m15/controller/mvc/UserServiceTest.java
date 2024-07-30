package pluralsight.m15.controller.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pluralsight.m15.domain.User;
import pluralsight.m15.domain.ValidationError;
import pluralsight.m15.repository.UserRepository;
import pluralsight.m15.security.MfaLoginClient;
import pluralsight.m15.security.MfaPasswordResetClient;
import pluralsight.m15.service.AccountLockedException;
import pluralsight.m15.service.UserService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    private UserService userService;
    private UserRepository userRepository;
    private Clock clock;
    private MfaLoginClient mfaLoginClient;
    private MfaPasswordResetClient mfaPasswordResetClient;
    private PasswordEncoder passwordEncoder;
    private CompromisedPasswordChecker compromisedPasswordChecker;

    @BeforeEach
    public void setUp() {
        userRepository = new UserRepository();
        clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
        mfaLoginClient = mock(MfaLoginClient.class);
        mfaPasswordResetClient = mock(MfaPasswordResetClient.class);
        passwordEncoder = new BCryptPasswordEncoder();
        compromisedPasswordChecker = mock(CompromisedPasswordChecker.class);
        userService = new UserService(userRepository,
                passwordEncoder,
                compromisedPasswordChecker,
                clock,
                mfaLoginClient,
                mfaPasswordResetClient);
    }

    @Test
    public void shouldSendLoginOtp() {
        userRepository.saveUser(User.builder().username("username").build());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        userService.triggerLoginOtp("username");

        verify(mfaLoginClient).sendLoginOtp(eq("username"), tokenCaptor.capture());

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
    public void verifyWrongLoginOtpTokenIsInvalid() {
        final User user = User.builder()
                .otpLoginToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .build();
        userRepository.saveUser(user);

        final boolean isValid = userService.verifyLoginOtp("wrong", "username");

        assertThat(isValid).isFalse();
        assertThat(user.getOtpLoginToken()).isPresent();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getLastFailedLoginTime()).contains(LocalDateTime.now(clock));
    }

    @Test
    public void verifyExpiredLoginOtpTokenIsInvalid() {
        final User user = User.builder()
                .otpLoginToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock).minusMinutes(10))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .build();
        userRepository.saveUser(user);

        final boolean isValid = userService.verifyLoginOtp("token", "username");

        assertThat(isValid).isFalse();
        assertThat(user.getOtpLoginToken()).isPresent();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getLastFailedLoginTime()).contains(LocalDateTime.now(clock));
    }

    @Test
    public void verifyUnexpiredMatchingLoginOtpTokenIsValid() {
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

        final boolean isValid = userService.verifyLoginOtp("token", "username");

        assertThat(isValid).isTrue();
        assertThat(user.getOtpLoginToken()).isEmpty();
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLastFailedLoginTime()).isEmpty();
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
    public void assertNotLockedAfter3AttemptsExactlyOneMinuteAgo() {
        User user = User.builder()
                .failedLoginAttempts(3)
                .lastFailedLoginTime(LocalDateTime.now(clock).minusMinutes(1))
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
                userService.verifyLoginOtp("token", "username");
            }
        }).isInstanceOf(AccountLockedException.class);

    }











    @Test
    public void shouldSendPasswordResetOtp() {
        userRepository.saveUser(User.builder().username("username").build());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        userService.triggerPasswordReset("username");

        verify(mfaPasswordResetClient).sendPasswordResetEmail(eq("username"), tokenCaptor.capture());

        final String tokenCaptorValue = tokenCaptor.getValue();

        assertThat(userService.getUserOrError("username").getPasswordResetToken())
                .isPresent()
                .hasValueSatisfying(userTokenHash -> {
                    assertThat(passwordEncoder.matches(tokenCaptorValue,
                            userTokenHash.getTokenHash())).isTrue();

                    assertThat(userTokenHash.getGeneratedAt())
                            .isEqualTo(LocalDateTime.now(clock));
                });
    }

    @Test
    public void verifyExpiredPasswordTokenIsInvalid() {
        final User user = User.builder()
                .passwordResetToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock).minusMinutes(10))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        userRepository.saveUser(user);

        final Set<ValidationError> validationErrors = userService.updatePassword(
                "token",
                "username",
                "nextpassword");

        assertThat(validationErrors).containsExactly(ValidationError.PASSWORD_RESET_INVALID);
        assertThat(user.getPasswordResetToken()).isNotEmpty();
        assertThat(passwordEncoder.matches("password", user.getPasswordHash()))
                .isTrue();
    }

    @Test
    public void verifyWrongLoginPasswordResetTokenIsInvalid() {
        final User user = User.builder()
                .passwordResetToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        userRepository.saveUser(user);

        final Set<ValidationError> validationErrors = userService.updatePassword(
                "username",
                "wrong",
                "nextpassword");

        assertThat(validationErrors).containsExactly(ValidationError.PASSWORD_RESET_INVALID);
        assertThat(user.getPasswordResetToken()).isNotEmpty();
        assertThat(passwordEncoder.matches("password", user.getPasswordHash()))
                .isTrue();
    }

    @Test
    public void verifyPasswordResetWithValidToken() {

        when(compromisedPasswordChecker.check("nextpassword"))
                .thenReturn(new CompromisedPasswordDecision(false));

        final User user = User.builder()
                .passwordResetToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock).minusMinutes(10).plusSeconds(1))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .lastFailedLoginTime(LocalDateTime.now(clock))
                .failedLoginAttempts(2)
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        userRepository.saveUser(user);

        final Set<ValidationError> validationErrors = userService.updatePassword(
                "username",
                "token",
                "nextpassword");

        assertThat(validationErrors).isEmpty();
        assertThat(user.getPasswordResetToken()).isEmpty();
        assertThat(passwordEncoder.matches("nextpassword", user.getPasswordHash()))
                .isTrue();
    }

    @Test
    public void verifyFailedPasswordResetWithValidTokenAndCompromisedNextPassword() {

        when(compromisedPasswordChecker.check("nextpassword"))
                .thenReturn(new CompromisedPasswordDecision(true));

        final User user = User.builder()
                .passwordResetToken(User.SecureToken.builder()
                        .generatedAt(LocalDateTime.now(clock).minusMinutes(10).plusSeconds(1))
                        .tokenHash(passwordEncoder.encode("token"))
                        .build())
                .username("username")
                .lastFailedLoginTime(LocalDateTime.now(clock))
                .failedLoginAttempts(2)
                .passwordHash(passwordEncoder.encode("password"))
                .build();
        userRepository.saveUser(user);

        final Set<ValidationError> validationErrors = userService.updatePassword(
                "username",
                "token",
                "nextpassword");

        assertThat(validationErrors).containsExactly(ValidationError.COMPROMISED_PASSWORD);
        assertThat(user.getPasswordResetToken()).isNotEmpty();
        assertThat(passwordEncoder.matches("password", user.getPasswordHash()))
                .isTrue();
    }
}
