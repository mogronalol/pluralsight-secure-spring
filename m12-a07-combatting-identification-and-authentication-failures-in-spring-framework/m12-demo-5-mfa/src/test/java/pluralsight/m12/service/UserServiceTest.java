package pluralsight.m12.service;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import pluralsight.m12.domain.User;
import pluralsight.m12.domain.ValidationError;
import pluralsight.m12.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository; // Not mocked, assumed to be interacting directly
    private Clock fixedClock;
    private final PasswordEncoder passwordEncoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private CompromisedPasswordChecker compromisedPasswordChecker;
    private EmailClient emailClient;

    @BeforeEach
    void setUp() {
        compromisedPasswordChecker = mock(CompromisedPasswordChecker.class);
        when(compromisedPasswordChecker.check(any())).thenReturn(new CompromisedPasswordDecision(false));
        emailClient = mock(EmailClient.class);
        userRepository = new UserRepository(); // This would need to be an actual class that interacts with the database.
        fixedClock = Clock.fixed(Instant.now(),ZoneId.systemDefault());
        userService = new UserService(userRepository, compromisedPasswordChecker,
                passwordEncoder, fixedClock, emailClient);
        userRepository.deleteAll();
    }

    @Test
    void testRecordFailedLoginAttempt() {
        String username = "user@example.com";
        User user = User.builder()
                .username(username)
                .failedLoginAttempts(1)
                .build();
        userRepository.save(user);

        userService.recordFailedLoginAttemptIfExists(username);

        User updatedUser = userRepository.getUser(username).orElseThrow();
        AssertionsForClassTypes.assertThat(updatedUser.getFailedLoginAttempts()).isEqualTo(2);
        AssertionsForClassTypes.assertThat(updatedUser.getLastFailedLoginTime()).contains(LocalDateTime.now(fixedClock));
    }

    @Test
    void testAssertUserNotLocked_NotLocked() {
        User user = User.builder()
                .failedLoginAttempts(4)
                .lastFailedLoginTime(LocalDateTime.now(fixedClock).minusMinutes(2))
                .build();
        userRepository.save(user);

        assertThatCode(() -> userService.assertUserNotLocked(user)).doesNotThrowAnyException();
    }

    @Test
    void testAssertUserNotLocked_IsLocked() {
        User user = User.builder()
                .failedLoginAttempts(4) // More than MAX_LOGIN_BEFORE_LOCK
                .lastFailedLoginTime(LocalDateTime.now(fixedClock))
                .build();
        userRepository.save(user);

        assertThatThrownBy(() -> userService.assertUserNotLocked(user))
                .isInstanceOf(RuntimeException.class) // Replace RuntimeException with your specific exception class
                .hasMessageContaining("locked"); // Optional, check for specific message content if necessary
    }

    @Test
    void shouldSendPasswordResetEmailIfUserExists() {

        final ArgumentCaptor<String> tokenCaptor =
                ArgumentCaptor.forClass(String.class);

        String email = "user@example.com";
        User user = User.builder()
                .username(email)
                .passwordResetToken(null)
                .build();
        userRepository.save(user);

        userService.triggerPasswordReset(email);

        verify(emailClient).sendPasswordResetEmail(eq(email), tokenCaptor.capture());

        AssertionsForClassTypes.assertThat(user.getPasswordResetToken())
                .isPresent()
                .hasValueSatisfying(token -> {
                    AssertionsForClassTypes.assertThat(passwordEncoder.matches(tokenCaptor.getValue(),
                            token.getTokenHash())).isTrue();
                    AssertionsForClassTypes.assertThat(token.getGeneratedAt()).isEqualTo(LocalDateTime.now(fixedClock));
                });
    }

    @Test
    void shouldSendNoPasswordResetEmailIfUserDoesNotExist() {

        userService.triggerPasswordReset("does-not-exist");

        verifyNoInteractions(emailClient);

        AssertionsForClassTypes.assertThat(userRepository.getUser("does-not-exist")).isNotPresent();
    }

    @Test
    void shouldSendOtpEmailIfUserExists() {

        final ArgumentCaptor<String> tokenCaptor =
                ArgumentCaptor.forClass(String.class);

        String email = "user@example.com";
        User user = User.builder()
                .username(email)
                .passwordResetToken(null)
                .build();
        userRepository.save(user);

        userService.triggerOtp(email);

        verify(emailClient).sendOtpEmail(eq(email), tokenCaptor.capture());

        AssertionsForClassTypes.assertThat(user.getOtpLoginToken())
                .isPresent()
                .hasValueSatisfying(token -> {
                    AssertionsForClassTypes.assertThat(passwordEncoder.matches(tokenCaptor.getValue(),
                            token.getTokenHash())).isTrue();
                    AssertionsForClassTypes.assertThat(token.getGeneratedAt()).isEqualTo(LocalDateTime.now(fixedClock));
                });
    }

    @Test
    void shouldSendNoOtpResetEmailIfUserDoesNotExist() {

        userService.triggerOtp("does-not-exist");

        verifyNoInteractions(emailClient);

        AssertionsForClassTypes.assertThat(userRepository.getUser("does-not-exist")).isNotPresent();
    }

    @Test
    void updatePasswordWhenUserDoesNotExist() {
        Set<ValidationError>
                errors = userService.updatePassword("nonexistent@example.com", "resetToken", "currentPassword", "newPassword");

        assertThat(errors).contains(ValidationError.WRONG_OR_EXPIRED_TOKEN);
    }

    @Test
    void updatePasswordWithWrongToken() {
        String email = "user@example.com";
        String storedToken = "storedToken";
        final User.SecureToken resetToken = new User.SecureToken(passwordEncoder.encode(storedToken), LocalDateTime.now());
        userRepository.save(User.builder()
                .username(email)
                .passwordHash(passwordEncoder.encode("currentPassword"))
                .passwordResetToken(resetToken)
                .build());

        Set<ValidationError> errors = userService.updatePassword(email, "wrongToken", "currentPassword", "newPassword");

        assertThat(errors).containsExactly(ValidationError.WRONG_OR_EXPIRED_TOKEN);
        assertThat(userRepository.getUser(email))
                .hasValueSatisfying(u -> assertThat(u.getPasswordResetToken()).contains(resetToken));
    }

    @Test
    void updatePasswordWithExpiredToken() {
        String email = "user@example.com";
        String validToken = "validToken";
        final User.SecureToken resetToken = new User.SecureToken(passwordEncoder.encode(validToken), LocalDateTime.now().minusDays(1));
        userRepository.save(User.builder()
                .username(email)
                .passwordHash(passwordEncoder.encode("currentPassword"))
                .passwordResetToken(resetToken)
                .build());

        Set<ValidationError> errors = userService.updatePassword(email, validToken, "currentPassword", "newPassword");

        assertThat(errors).contains(ValidationError.WRONG_OR_EXPIRED_TOKEN);
        assertThat(userRepository.getUser(email))
                .hasValueSatisfying(u -> assertThat(u.getPasswordResetToken()).contains(resetToken));
    }

    @Test
    void updatePasswordWithEmailWhichDoesNotMatchToken() {
        String email = "user@example.com";
        String validToken = "validToken";
        final User.SecureToken resetToken =
                new User.SecureToken(passwordEncoder.encode(validToken), LocalDateTime.now());
        User user = User.builder()
                .username(email)
                .passwordHash(passwordEncoder.encode("currentPassword"))
                .passwordResetToken(resetToken)
                .build();
        userRepository.save(user);

        when(compromisedPasswordChecker.check("newPassword")).thenReturn(new CompromisedPasswordDecision(false));

        Set<ValidationError> errors = userService.updatePassword("other", validToken,
                "currentPassword", "newPassword");

        assertThat(errors).contains(ValidationError.WRONG_OR_EXPIRED_TOKEN);
        assertThat(userRepository.getUser(email))
                .hasValueSatisfying(u -> assertThat(u.getPasswordResetToken()).contains(resetToken));
    }

    @Test
    void updatePasswordWithWrongCurrentPassword() {
        String email = "user@example.com";
        String validToken = "validToken";
        final User.SecureToken resetToken =
                new User.SecureToken(passwordEncoder.encode(validToken), LocalDateTime.now());
        User user = User.builder()
                .username(email)
                .passwordHash(passwordEncoder.encode("currentPassword"))
                .passwordResetToken(resetToken)
                .build();
        userRepository.save(user);

        Set<ValidationError> errors = userService.updatePassword(email, validToken,
                "other", "newPassword");

        assertThat(errors).contains(ValidationError.WRONG_PASSWORD);
        assertThat(userRepository.getUser(email))
                .hasValueSatisfying(u -> assertThat(u.getPasswordResetToken()).contains(resetToken));
    }

    @Test
    void updatePasswordWithWrongCompromisedPassword() {
        String email = "user@example.com";
        String validToken = "validToken";
        final User.SecureToken resetToken =
                new User.SecureToken(passwordEncoder.encode(validToken), LocalDateTime.now());
        User user = User.builder()
                .username(email)
                .passwordHash(passwordEncoder.encode("currentPassword"))
                .passwordResetToken(resetToken)
                .build();
        userRepository.save(user);

        when(compromisedPasswordChecker.check("newPassword")).thenReturn(new CompromisedPasswordDecision(true));

        Set<ValidationError> errors = userService.updatePassword(email, validToken,
                "currentPassword", "newPassword");

        assertThat(errors).contains(ValidationError.PASSWORD_COMPROMISED);
        assertThat(userRepository.getUser(email))
                .hasValueSatisfying(u -> assertThat(u.getPasswordResetToken()).contains(resetToken));
    }

    @Test
    void updatePasswordSuccessfullyAndDeleteToken() {
        String email = "user@example.com";
        String validToken = "validToken";
        User user = User.builder()
                .username(email)
                .passwordHash(passwordEncoder.encode("currentPassword"))
                .passwordResetToken(new User.SecureToken(passwordEncoder.encode(validToken), LocalDateTime.now()))
                .build();
        userRepository.save(user);

        Set<ValidationError> errors = userService.updatePassword(email, validToken, "currentPassword", "newPassword");

        assertThat(errors).isEmpty();
        assertThat(userRepository.getUser(email))
                .hasValueSatisfying(u -> assertThat(u.getPasswordResetToken()).isNotPresent());
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
        userRepository.save(User.builder()
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
        userRepository.save(User.builder()
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
        userRepository.save(User.builder()
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
                    assertThat(u.getPasswordResetToken()).isEmpty();
                    assertThat(u.getFailedLoginAttempts()).isEqualTo(0);
                    assertThat(u.getLastFailedLoginTime()).isEmpty();
                });
    }
}