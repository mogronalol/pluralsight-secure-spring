package pluralsight.m12.controller.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.test.web.servlet.MockMvc;
import pluralsight.m12.repository.UserRepository;
import pluralsight.m12.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ResetPasswordControllerIntegrationTest {

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_CURRENT_PASSWORD = "CurrentValid123!";
    private static final String VALID_NEW_PASSWORD = "NewValid123!";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CompromisedPasswordChecker compromisedPasswordChecker;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        when(compromisedPasswordChecker.check(any())).thenReturn(
                new CompromisedPasswordDecision(false));

        userRepository.deleteAll();
        userService.createUser(VALID_EMAIL, VALID_CURRENT_PASSWORD);
    }

    @Test
    void whenNewPasswordAndConfirmPasswordDoNotMatch_thenShowsFormWithErrors()
            throws Exception {
        mockMvc.perform(post("/reset-password")
                        .param("email", VALID_EMAIL)
                        .param("currentPassword", VALID_CURRENT_PASSWORD)
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

        mockMvc.perform(post("/reset-password")
                        .param("email", VALID_EMAIL)
                        .param("currentPassword", VALID_CURRENT_PASSWORD)
                        .param("newPassword", "Compromised123!")
                        .param("confirmPassword", "Compromised123!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("passwordForm", "newPassword"))
                .andExpect(view().name("reset-password"));
    }

    @Test
    void whenCurrentPasswordIsIncorrect_thenShowsFormWithErrors() throws Exception {
        // Assuming the service detects the incorrect password
        mockMvc.perform(post("/reset-password")
                        .param("email", VALID_EMAIL)
                        .param("newPassword", VALID_NEW_PASSWORD)
                        .param("confirmPassword", VALID_NEW_PASSWORD)
                        .param("currentPassword", "Incorrect123!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("passwordForm", "currentPassword"))
                .andExpect(view().name("reset-password"));
    }

    @Test
    void whenEmailDoesNotExist_thenShowsFormWithErrors() throws Exception {

        mockMvc.perform(post("/reset-password")
                        .param("email", "nonexistent@example.com")
                        .param("currentPassword", VALID_CURRENT_PASSWORD)
                        .param("newPassword", VALID_NEW_PASSWORD)
                        .param("confirmPassword", VALID_NEW_PASSWORD)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("passwordForm", "email"))
                .andExpect(view().name("reset-password"));
    }

    @Test
    void givenValidInputs_thenRedirectToLogin() throws Exception {
        mockMvc.perform(post("/reset-password")
                        .param("email", VALID_EMAIL)
                        .param("currentPassword", VALID_CURRENT_PASSWORD)
                        .param("newPassword", VALID_NEW_PASSWORD)
                        .param("confirmPassword", VALID_NEW_PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("success",
                        "Your password has been successfully reset."));
    }
}

