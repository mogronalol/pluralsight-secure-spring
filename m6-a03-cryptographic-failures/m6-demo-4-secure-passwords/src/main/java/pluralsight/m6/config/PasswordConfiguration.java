package pluralsight.m6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.Md4PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        final DelegatingPasswordEncoder delegatingPasswordEncoder =
                (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new Md4PasswordEncoder());
        return delegatingPasswordEncoder;
    }
}
