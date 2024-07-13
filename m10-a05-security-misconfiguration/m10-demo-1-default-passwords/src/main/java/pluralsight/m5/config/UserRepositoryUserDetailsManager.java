package pluralsight.m5.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import pluralsight.m5.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserRepositoryUserDetailsManager implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException {
        return userRepository.findById(username);
    }
}
