package pluralsight.m14.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authorization.event.AuthorizationEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthorizationEventListener
        implements ApplicationListener<AuthorizationEvent> {

    @Override
    public void onApplicationEvent(AuthorizationEvent event) {
        log.info("Authorization event {}", event);
    }
}
