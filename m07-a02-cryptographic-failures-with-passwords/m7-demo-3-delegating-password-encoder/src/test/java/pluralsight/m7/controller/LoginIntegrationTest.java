package pluralsight.m7.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.userdetails.User;
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

        final String username = createTestUser();

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

        final String username = createTestUser();

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

        final String user1 = createTestUser();

        final String user1Password = userDetailsManager.loadUserByUsername(user1).getPassword();

        assertThat(user1Password)
                // Was not stored as plaintext
                .doesNotContain("password")
                .startsWith("{bcrypt}");

        final String user2 = createTestUser();

        final String user2Password = userDetailsManager.loadUserByUsername(user2).getPassword();

        assertThat(user2Password)
                // Was not stored as plaintext
                .doesNotContain("password")
                .startsWith("{bcrypt}");

        assertThat(user2Password).isNotEqualTo(user1Password);
    }

    @Test
    public void shouldUpgradeLegacyEncoding() throws Exception {

        final String encodedPassword = new Md4PasswordEncoder().encode(
                "password");
        createTestUserWithPasswordHash("username", "{MD4}" + encodedPassword);

        final String md4Hash =
                userDetailsManager.loadUserByUsername("username").getPassword();

        login("username", "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        final String upgradePassword =
                userDetailsManager.loadUserByUsername("username").getPassword();

        assertThat(upgradePassword)
                .doesNotContain(md4Hash)
                .startsWith("{bcrypt}");
    }

    @Test
    public void shouldUpgradeLegacyEncodingWithoutPrefix() throws Exception {

        final String encodedPassword = new Md4PasswordEncoder().encode(
                "password");
        createTestUserWithPasswordHash("username", encodedPassword);

        final String md4Hash =
                userDetailsManager.loadUserByUsername("username").getPassword();

        login("username", "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        final String upgradePassword =
                userDetailsManager.loadUserByUsername("username").getPassword();

        assertThat(upgradePassword)
                .doesNotContain(md4Hash)
                .startsWith("{bcrypt}");
    }

    private String createTestUser() {

        final String username = "user-" + UUID.randomUUID();

        createTestUserWithPasswordHash(username, passwordEncoder.encode("password"));

        return username;
    }

    private void createTestUserWithPasswordHash(final String username, final String encode) {

        userDetailsManager.createUser(User.withUsername(username)
                .password(encode)
                .roles("USER")
                .build());
    }

    private ResultActions login(final String username, final String password)
            throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("username", username)
                .param("password", password)
                .with(csrf()));
    }
}

