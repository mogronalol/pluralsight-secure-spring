package pluralsight.m7.controller;

import lombok.SneakyThrows;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

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

        final String username = createTestUserWithPasswordHash(
                passwordEncoder.encode("password"));

        canLoginWithCorrectPassword(username, "password");
    }

    @Test
    public void testLogInWithWrongPassword() throws Exception {

        final String username = createTestUserWithPasswordHash(
                passwordEncoder.encode("password"));

        cannotLoginWithWrongPassword(username, "wrong-password");
    }

    @Test
    public void shouldStorePasswordHash() {
        final String username = createTestUserWithPasswordHash(
                passwordEncoder.encode("password"));
        final String password = userDetailsManager.loadUserByUsername(username).getPassword();
        assertThat(password).doesNotContain("password");
    }

    @Test
    public void shouldStoreDifferentHashesForTheSamePassword() {
        final String user1 = createTestUserWithPasswordHash(
                passwordEncoder.encode("password"));
        final String user2 = createTestUserWithPasswordHash(
                passwordEncoder.encode("password"));

        final String password1 = userDetailsManager.loadUserByUsername(user1).getPassword();
        final String password2 = userDetailsManager.loadUserByUsername(user2).getPassword();

        assertThat(password1).doesNotContain(password2);
    }

    @Test
    public void shouldUpgradeLegacyEncoding() {
        final String md4Hash = new Md4PasswordEncoder().encode("password");

        final String username = createTestUserWithPasswordHash("{MD4}" + md4Hash);

        canLoginWithCorrectPassword(username, "password");

        final String upgradedPassword = userDetailsManager.loadUserByUsername(username)
                .getPassword();

        assertThat(upgradedPassword)
                .doesNotContain(md4Hash)
                .startsWith("{bcrypt}");
    }

    @Test
    public void shouldUpgradeLegacyEncodingWithoutPrefix() {
        final String md4Hash = new Md4PasswordEncoder().encode("password");

        final String username = createTestUserWithPasswordHash(md4Hash);

        canLoginWithCorrectPassword(username, "password");

        final String upgradedPassword = userDetailsManager.loadUserByUsername(username)
                .getPassword();

        assertThat(upgradedPassword)
                .doesNotContain(md4Hash)
                .startsWith("{bcrypt}");
    }

    private String createTestUserWithPasswordHash(final String encode) {

        final String username = UUID.randomUUID().toString();

        userDetailsManager.createUser(User.withUsername(username)
                .password(encode)
                .roles("USER")
                .build());

        return username;
    }


    @SneakyThrows
    private void canLoginWithCorrectPassword(final String username,
                                             final String password)  {
        final MvcResult mvcResult = mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Logged in")));
    }

    private void cannotLoginWithWrongPassword(final String username, final String password) throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}

