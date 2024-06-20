package pluralsight.m12.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;
import pluralsight.m12.service.UserService;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener implements
        ApplicationListener<AbstractAuthenticationFailureEvent> {

    private final UserService userService;

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        userService.recordFailedLoginAttemptIfExists(event.getAuthentication().getName());
    }
}