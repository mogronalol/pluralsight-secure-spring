package pluralsight.m13.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UserService userService;

    @Override
    public void onApplicationEvent(final AuthenticationSuccessEvent event) {
        final String username =
                ((UserDetails) event.getAuthentication().getPrincipal()).getUsername();
        userService.triggerLoginOtp(username);
    }
}