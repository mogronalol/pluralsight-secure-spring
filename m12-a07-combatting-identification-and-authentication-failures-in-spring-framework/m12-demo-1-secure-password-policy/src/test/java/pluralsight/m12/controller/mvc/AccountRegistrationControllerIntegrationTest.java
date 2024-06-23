package pluralsight.m12.controller.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pluralsight.m12.domain.User;
import pluralsight.m12.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountRegistrationControllerIntegrationTest {

    public static final String SIXTY_FIVE_CHARACTER_PASSWORD = """
            1234567891
            1234567891
            1234567891
            1234567891
            1234567891
            1234567891
            12345
            """;
    public static final String COMPROMISED_PASSWORD = "CompromisedPassword";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CompromisedPasswordChecker compromisedPasswordChecker;

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_PASSWORD = "ValidPass123!";

    @BeforeEach
    public void resetUsers() {
        userRepository.deleteAll();
        when(compromisedPasswordChecker.check(any())).thenReturn(new CompromisedPasswordDecision(false));
    }

    @Test
    void givenValidRegistrationThenRedirectToLoginAndSaveUser() throws Exception {
        mockMvc.perform(post("/account-registration")
                        .param("email", VALID_EMAIL)
                        .param("password", VALID_PASSWORD)
                        .param("confirmPassword", VALID_PASSWORD)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("registrationSuccess", "Registration successful. Please login."));


        assertThat(userRepository.getUser(VALID_EMAIL)
                .map(User::getPasswordHash)
                .map(h -> passwordEncoder.matches(VALID_PASSWORD, h))
                .orElse(false)
        ).isTrue();
    }

    @Test
    void whenRegisterWithBlankEmail_thenShowsFormWithErrors() throws Exception {
        mockMvc.perform(post("/account-registration")
                        .param("email", " ")
                        .param("password", VALID_PASSWORD)
                        .param("confirmNothing", VALID_PASSWORD)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userForm", "email"))
                .andExpect(view().name("account-registration"));

        assertThat(userRepository.getUser(VALID_EMAIL)).isEmpty();
    }

    @Test
    void whenRegisterWithInvalidEmail_thenShowsFormWithErrors() throws Exception {
        mockMvc.perform(post("/account-registration")
                        .param("email", "foo@foo@foo")
                        .param("password", VALID_PASSWORD)
                        .param("confirmNothing", VALID_PASSWORD)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userForm", "email"))
                .andExpect(view().name("account-registration"));

        assertThat(userRepository.getUser("foo@foo@foo")).isEmpty();
    }

    @Test
    void whenPasswordsDoNotMatch_thenShowsFormWithErrors() throws Exception {
        mockMvc.perform(post("/account-registration")
                        .param("email", VALID_EMAIL)
                        .param("password", VALID_PASSWORD)
                        .param("confirmPassword", "DifferentPassword123!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userForm", "password"))
                .andExpect(view().name("account-registration"));

        assertThat(userRepository.getUser(VALID_EMAIL)).isEmpty();
    }

    @Test
    void whenPasswordsLessThan8Characters_thenShowsFormWithErrors() throws Exception {
        mockMvc.perform(post("/account-registration")
                        .param("email", VALID_EMAIL)
                        .param("password", "1234567")
                        .param("confirmPassword", "DifferentPassword123!")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userForm", "password"))
                .andExpect(view().name("account-registration"));

        assertThat(userRepository.getUser(VALID_EMAIL)).isEmpty();
    }

    @Test
    void whenPasswordsMoreThan64Characters_thenShowsFormWithErrors() throws Exception {
        mockMvc.perform(post("/account-registration")
                        .param("email", VALID_EMAIL)
                        .param("password", SIXTY_FIVE_CHARACTER_PASSWORD.stripIndent())
                        .param("confirmPassword", SIXTY_FIVE_CHARACTER_PASSWORD)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userForm", "password"))
                .andExpect(view().name("account-registration"));

        assertThat(userRepository.getUser(VALID_EMAIL)).isEmpty();
    }

    @Test
    void whenPasswordIsCompromised_thenShowsFormWithErrors() throws Exception {

        when(compromisedPasswordChecker.check(COMPROMISED_PASSWORD)).thenReturn(new CompromisedPasswordDecision(true));

        mockMvc.perform(post("/account-registration")
                        .param("email", VALID_EMAIL)
                        .param("password", COMPROMISED_PASSWORD)
                        .param("confirmPassword", COMPROMISED_PASSWORD)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userForm", "password"))
                .andExpect(view().name("account-registration"));

        assertThat(userRepository.getUser(VALID_EMAIL)).isEmpty();
    }
}
