package pluralsight.m12.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pluralsight.m12.domain.User;
import pluralsight.m12.repository.UserRepository;
import pluralsight.m12.security.Roles;

import java.util.Set;

@Configuration
public class TestUserCreator {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void postConstruct() {
        userRepository.save(User.builder()
                .username("test@test.com")
                .passwordHash(passwordEncoder.encode("password-password-password"))
                .roles(Set.of(Roles.CUSTOMER_SERVICE_MANAGER))
                .build());
    }

}
