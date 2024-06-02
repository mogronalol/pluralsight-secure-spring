package pluralsight.m6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.Md4PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Configuration
public class PasswordConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        final Md4PasswordEncoder md4PasswordEncoder = new Md4PasswordEncoder();

        final DelegatingPasswordEncoder delegatingPasswordEncoder =
                new DelegatingPasswordEncoder("bcrypt-12", Map.of(
                        "bcrypt", new BCryptPasswordEncoder(),
                        "bcrypt-12", new BCryptPasswordEncoder(12),
                        "MD4", md4PasswordEncoder)
                );
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(md4PasswordEncoder);
        return delegatingPasswordEncoder;
    }
}
