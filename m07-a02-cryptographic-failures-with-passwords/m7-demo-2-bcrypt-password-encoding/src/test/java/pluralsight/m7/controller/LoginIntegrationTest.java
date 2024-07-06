package pluralsight.m7.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testLogInWithCorrectPassword() throws Exception {

        createUser("username", "password");

        canLoginWithCorrectPassword("username", "password");
    }

    @Test
    public void testLogInWithWrongPassword() throws Exception {

        createUser("username", "password");

        cannotLoginWithWrongPassword("wrong-password");
    }

    @Test
    public void shouldStorePasswordHash() {
        createUser("username1", "password");

        final String user1Password = userDetailsManager.loadUserByUsername("username1").getPassword();

        assertThat(user1Password)
                // Was not stored as plaintext
                .doesNotContain("password")
                .startsWith("{bcrypt}");
    }

    @Test
    public void shouldStoreDifferentHashesForTheSamePassword(final String password) {

        createUser("username1", "password");
        createUser("username2", "password");

        final String user1Password =
                userDetailsManager.loadUserByUsername("username1").getPassword();
        final String user2Password =
                userDetailsManager.loadUserByUsername("username2").getPassword();

        assertThat(user2Password).isNotEqualTo(user1Password);
    }

    private ResultActions login(final String username, final String password)
            throws Exception {
        return mockMvc.perform(post("/login")
                .param("username", username)
                .param("password", password)
                .with(csrf()));
    }

    private void canLoginWithCorrectPassword(final String username,
                                             final String password) throws Exception {
        final MvcResult mvcResult = login(username, password)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logged in")));
    }

    private void cannotLoginWithWrongPassword(final String username) throws Exception {
        final MvcResult mvcResult = login(username, "wrong")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    private void createUser(final String username, final String password) {

        userDetailsManager.createUser(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build());
    }
}

