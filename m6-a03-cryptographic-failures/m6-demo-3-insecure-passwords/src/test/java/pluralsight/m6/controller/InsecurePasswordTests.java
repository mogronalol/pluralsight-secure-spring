package pluralsight.m6.controller;

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

    private final InSecureMD5PasswordEncoder insecurePasswordEncoder =
            new InSecureMD5PasswordEncoder();

    private final UserDetailsManager userDetailsManager = new InMemoryUserDetailsManager();

    @Test
    public void plaintextPasswordsAreInSecure() throws Exception {

        final String username = createTestUser("password");

        final UserDetails userDetails = userDetailsManager.loadUserByUsername(username);

        assertThat(userDetails.getPassword())
                // Was not stored as plaintext
                .isEqualTo("password");
    }

    @Test
    public void md5IsInSecureWithoutSaltingIsSusceptibleToRainbowTables() {

        final String firstUser = createTestUser(
                insecurePasswordEncoder.encodeWithoutSalt("password"));

        final String firstPassword =
                userDetailsManager.loadUserByUsername(firstUser).getPassword();

        final String secondUser = createTestUser(
                insecurePasswordEncoder.encodeWithoutSalt("password"));

        final String secondPassword =
                userDetailsManager.loadUserByUsername(secondUser).getPassword();

        assertThat(firstPassword).isEqualTo(secondPassword);

        assertThat(RAINBOW_TABLE_OF_MD_5)
                .containsEntry(firstPassword, "password");
    }

    @Test
    public void mdfCanBeEasilyBruteforcedWithSalting() {

        final String encodedWithSalt = insecurePasswordEncoder.encodeWithSalt("password",
                "salt");

        final String salt =
                encodedWithSalt.substring(1, encodedWithSalt.indexOf("}"));

        final Optional<String> any = Arrays.stream(COMMON_PASSWORDS)
                .filter(p -> insecurePasswordEncoder.encodeWithSalt(p, salt)
                        .equals(encodedWithSalt))
                .findAny();

        assertThat(any).contains("password");
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

