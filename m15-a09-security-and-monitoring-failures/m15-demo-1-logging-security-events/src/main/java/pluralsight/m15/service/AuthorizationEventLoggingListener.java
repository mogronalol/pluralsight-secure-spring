package pluralsight.m15.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.access.event.AbstractAuthorizationEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthorizationEventLoggingListener implements ApplicationListener<AbstractAuthorizationEvent> {
    @Override
    public void onApplicationEvent(final AbstractAuthorizationEvent event) {
        log.info("Authorization event received {}", event);
    }
}
