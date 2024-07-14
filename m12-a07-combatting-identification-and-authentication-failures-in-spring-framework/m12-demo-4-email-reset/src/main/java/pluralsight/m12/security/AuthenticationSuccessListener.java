package pluralsight.m12.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pluralsight.m12.service.UserService;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements
        ApplicationListener<AuthenticationSuccessEvent> {
    private final UserService userService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String username = ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        userService.resetFailedAttempts(username);
    }
}
