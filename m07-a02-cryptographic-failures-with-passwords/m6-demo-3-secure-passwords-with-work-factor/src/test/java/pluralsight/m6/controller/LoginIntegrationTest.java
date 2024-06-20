package pluralsight.m6.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.Md4PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

        final String username = createTestUser("password");

        final MvcResult mvcResult = login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logged in")));
    }

    @Test
    public void testLogInWithWrongPassword() throws Exception {

        final String username = createTestUser("password");

        final MvcResult mvcResult = login(username, "wrong")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    public void shouldUseSaltedBcryptByDefault() {

        final String user1 = createTestUser("password");

        final String user1Password = userDetailsManager.loadUserByUsername(user1).getPassword();

        assertThat(user1Password)
                // Was not stored as plaintext
                .doesNotContain("password")
                .startsWith("{bcrypt-12}");

        final String user2 = createTestUser("password");

        final String user2Password = userDetailsManager.loadUserByUsername(user2).getPassword();

        assertThat(user2Password)
                // Was not stored as plaintext
                .doesNotContain("password")
                .startsWith("{bcrypt-12}");

        assertThat(user2Password).isNotEqualTo(user1Password);
    }

    @Test
    public void shouldUpgradeLegacyEncodingWithoutPrefix() throws Exception {

        final String username =
                createTestUserWithDifferentPasswordEncoder(new Md4PasswordEncoder().encode(
                        "password"));
        login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        final String upgradePassword =
                userDetailsManager.loadUserByUsername(username).getPassword();

        assertThat(upgradePassword)
                .startsWith("{bcrypt-12}")
                .isNotEqualTo("{MD4}");
    }

    @Test
    public void shouldUpgradeBcryptWorkFactor() throws Exception {

        final String username =
                createTestUserWithDifferentPasswordEncoder("{bcrypt}" + new BCryptPasswordEncoder().encode("password"));

        login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        final String upgradePassword =
                userDetailsManager.loadUserByUsername(username).getPassword();

        assertThat(upgradePassword)
                .startsWith("{bcrypt-12}");
    }

    private String createTestUser(final String password) {

        final String username = "user-" + UUID.randomUUID();

        userDetailsManager.createUser(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build());

        return username;
    }

    private String createTestUserWithDifferentPasswordEncoder(final String password) {

        final String username = "user-" + UUID.randomUUID();

        userDetailsManager.createUser(User.withUsername(username)
                .password(password)
                .roles("USER")
                .build());

        return username;
    }

    private ResultActions login(final String username, final String password)
            throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("username", username)
                .param("password", password)
                .with(csrf()));
    }
}

