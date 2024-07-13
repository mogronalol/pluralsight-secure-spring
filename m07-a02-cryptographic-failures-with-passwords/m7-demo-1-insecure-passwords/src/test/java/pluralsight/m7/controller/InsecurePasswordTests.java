package pluralsight.m7.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class InsecurePasswordTests {

    public static final String[] COMMON_PASSWORDS =
            {"123456", "12345678", "qwerty", "abc123", "password", "monkey",
                    "letmein", "dragon"};

    public static final Map<String, String> RAINBOW_TABLE_OF_MD_5 = Stream.of(COMMON_PASSWORDS)
            .collect(Collectors.toMap(DigestUtils::md5Hex, k -> k));

    private final InSecureMD5PasswordEncoder md5PasswordEncoder =
            new InSecureMD5PasswordEncoder();

    private final UserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

    @Test
    public void plainTextPasswordsAreInsecure() {
        final String username = createTestUser("password");

        final UserDetails userDetails = userDetailsManager.loadUserByUsername(username);

        assertThat(userDetails.getPassword()).isEqualTo("password");
    }

    @Test
    public void md5IsVulnerableWithoutSaltingToRainbowTables() {
        final String firstUser = createTestUser(md5PasswordEncoder.encodeWithoutSalt(
                "password"));

        final String firstPassword = userDetailsManager.loadUserByUsername(firstUser)
                .getPassword();

        assertThat(firstPassword).isNotEqualTo("password");

        final String secondUser = createTestUser(md5PasswordEncoder.encodeWithoutSalt(
                "password"));

        final String secondPassword = userDetailsManager.loadUserByUsername(secondUser)
                .getPassword();

        assertThat(secondPassword).isNotEqualTo("password");

        assertThat(firstPassword).isEqualTo(secondPassword);

        assertThat(RAINBOW_TABLE_OF_MD_5)
                .containsEntry(firstPassword, "password");
    }

    @Test
    public void md5CanEasilyBeBruteforced() {
        final String user1 =
                createTestUser(md5PasswordEncoder.encodeWithSalt("password", "salt1"));

        final String user2 =
                createTestUser(md5PasswordEncoder.encodeWithSalt("password", "salt2"));

        final String user1Password = userDetailsManager.loadUserByUsername(user1).getPassword();

        final String user2Password =
                userDetailsManager.loadUserByUsername(user2).getPassword();

        assertThat(user1Password).isNotEqualTo(user2Password);

        final String salt = user1Password.substring(1, user1Password.indexOf("}"));

        final Optional<String> bruteForced = Arrays.stream(COMMON_PASSWORDS)
                .filter(p -> md5PasswordEncoder.encodeWithSalt(p, salt)
                        .equals(user1Password))
                .findAny();

        assertThat(bruteForced).contains("password");
    }


    private String createTestUser(final String password) {

        final String username = "user-" + UUID.randomUUID();

        userDetailsManager.createUser(User.withUsername(username)
                .password(password)
                .roles("USER")
                .build());

        return username;
    }
}

