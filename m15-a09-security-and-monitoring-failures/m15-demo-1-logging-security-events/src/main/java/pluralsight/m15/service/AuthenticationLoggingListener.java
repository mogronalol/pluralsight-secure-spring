package pluralsight.m15.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationLoggingListener implements ApplicationListener<AbstractAuthenticationEvent> {
    @Override
    public void onApplicationEvent(final AbstractAuthenticationEvent event) {
        log.info("Authentication event received {}", event);
    }
}
