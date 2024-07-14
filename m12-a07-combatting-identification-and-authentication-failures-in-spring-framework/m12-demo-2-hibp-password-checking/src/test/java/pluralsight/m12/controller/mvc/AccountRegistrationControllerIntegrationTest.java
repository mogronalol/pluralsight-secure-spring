package pluralsight.m12.controller.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pluralsight.m12.domain.User;
import pluralsight.m12.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountRegistrationControllerIntegrationTest {

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_PASSWORD = "ValidPass123!";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void resetUsers() {
        userRepository.deleteAll();
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
    void rejectLessThen6CharacterPasswords() throws Exception {
        assertPasswordInvalid("12311");
    }

    @Test
    void rejectMoreThen64CharacterPasswords() throws Exception {
        assertPasswordInvalid(
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "1111111111" +
                "11111"
        );
    }

    @Test
    void canRegisterWith8CharacterPassword() throws Exception {
        assertRegistrationPossibleWithPassword("1A@b1111");
    }

    @Test
    void canRegisterWith64CharacterPassword() throws Exception {
        assertRegistrationPossibleWithPassword("1111111111" +
                                               "1111111111" +
                                               "1111111111" +
                                               "1111111111" +
                                               "1111111111" +
                                               "1111111111" +
                                               "1111");
    }

    private void assertRegistrationPossibleWithPassword(final String validPassword)
            throws Exception {
        mockMvc.perform(post("/account-registration")
                        .param("email", VALID_EMAIL)
                        .param("password", validPassword)
                        .param("confirmPassword", validPassword)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("registrationSuccess",
                        "Registration successful. Please login."));


        assertThat(userRepository.getUser(VALID_EMAIL)
                .map(User::getPasswordHash)
                .map(h -> passwordEncoder.matches(validPassword, h))
                .orElse(false)
        ).isTrue();
    }

    private void assertPasswordInvalid(final String invalidPassword) throws Exception {
        mockMvc.perform(post("/account-registration")
                        .param("email", VALID_EMAIL)
                        .param("password", invalidPassword)
                        .param("confirmPassword", invalidPassword)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userForm", "password"))
                .andExpect(view().name("account-registration"));

        assertThat(userRepository.getUser(VALID_EMAIL)).isEmpty();
    }
}
