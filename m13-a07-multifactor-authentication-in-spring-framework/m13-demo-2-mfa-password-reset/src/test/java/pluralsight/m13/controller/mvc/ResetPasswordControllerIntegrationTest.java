package pluralsight.m13.controller.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pluralsight.m13.repository.UserRepository;
import pluralsight.m13.security.MfaPasswordResetClient;
import pluralsight.m13.service.UserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ResetPasswordControllerIntegrationTest {

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_NEW_PASSWORD = "NewValid123!";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CompromisedPasswordChecker compromisedPasswordChecker;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @MockBean
    private MfaPasswordResetClient mfaPasswordResetClient;

    @BeforeEach
    public void setup() {
        when(compromisedPasswordChecker.check(any())).thenReturn(
                new CompromisedPasswordDecision(false));

        userRepository.deleteAll();
        userService.createUser(VALID_EMAIL, "password");
    }

    @Test
    void whenNewPasswordAndConfirmPasswordDoNotMatch_thenShowsFormWithErrors()
            throws Exception {

        final String resetToken = requestPasswordResetAndCaptureToken();

        mockMvc.perform(post("/reset-password")
                        .param("email", VALID_EMAIL)
                        .param("resetToken", resetToken)
                        .param("newPassword", VALID_NEW_PASSWORD)
                        .param("confirmPassword", "Mismatch123!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("passwordForm", "confirmPassword"))
                .andExpect(view().name("reset-password"));
    }

    @Test
    void whenPasswordIsCompromised_thenShowsFormWithErrors() throws Exception {

        when(compromisedPasswordChecker.check("Compromised123!")).thenReturn(
                new CompromisedPasswordDecision(true));

        final String resetToken = requestPasswordResetAndCaptureToken();

        mockMvc.perform(post("/reset-password")
                        .param("email", VALID_EMAIL)
                        .param("resetToken", resetToken)
                        .param("newPassword", "Compromised123!")
                        .param("confirmPassword", "Compromised123!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("passwordForm", "newPassword"))
                .andExpect(view().name("reset-password"));
    }

    @Test
    void whenEmailDoesNotExist_thenShowsFormWithErrors() throws Exception {

        final String resetToken = requestPasswordResetAndCaptureToken();

        mockMvc.perform(post("/reset-password")
                        .param("email", "nonexistent@example.com")
                        .param("resetToken", resetToken)
                        .param("newPassword", VALID_NEW_PASSWORD)
                        .param("confirmPassword", VALID_NEW_PASSWORD)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("passwordForm", "resetToken"))
                .andExpect(view().name("reset-password"));
    }

    @Test
    void shouldResetWithValidInputs() throws Exception {

        final String resetToken = requestPasswordResetAndCaptureToken();

        mockMvc.perform(post("/reset-password")
                        .param("email", VALID_EMAIL)
                        .param("resetToken", resetToken)
                        .param("newPassword", VALID_NEW_PASSWORD)
                        .param("confirmPassword", VALID_NEW_PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("success",
                        "Your password has been successfully reset."));

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("username", VALID_EMAIL)
                .param("password", VALID_NEW_PASSWORD)
                .with(csrf()))
                .andExpect(redirectedUrl("/login/otp"));
    }

    private String requestPasswordResetAndCaptureToken() throws Exception {
        mockMvc.perform(post("/reset-password/initiate")
                        .param("email", VALID_EMAIL)
                        .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("we will send a password reset " +
                                                           "link to that email")));

        final ArgumentCaptor<String> tokenCaptor =
                ArgumentCaptor.forClass(String.class);
        verify(mfaPasswordResetClient).sendPasswordResetEmail(eq(VALID_EMAIL),
                tokenCaptor.capture());
        assertThat(tokenCaptor.getValue()).isNotNull();
        return tokenCaptor.getValue();
    }

    @Test
    void whenTokenIsInvalid_thenShowsFormWithErrors() throws Exception {

        mockMvc.perform(post("/reset-password")
                        .param("email", VALID_EMAIL)
                        .param("resetToken", "invalid")
                        .param("newPassword", VALID_NEW_PASSWORD)
                        .param("confirmPassword", VALID_NEW_PASSWORD)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("passwordForm", "resetToken"))
                .andExpect(view().name("reset-password"));
    }
}

