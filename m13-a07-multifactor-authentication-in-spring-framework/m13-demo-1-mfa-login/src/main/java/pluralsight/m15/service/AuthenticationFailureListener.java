package pluralsight.m15.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {
    private final UserService userService;

    @Override
    public void onApplicationEvent(final AbstractAuthenticationFailureEvent event) {
        final String username = event.getAuthentication().getName();
        userService.recordFailedLoginAttemptIfExists(username);
    }
}
