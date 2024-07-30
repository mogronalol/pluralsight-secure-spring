package pluralsight.m15.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MfaPasswordResetClient {
    public void sendPasswordResetEmail(final String username, final String token) {
        log.info(("Sending password reset email to %s with token link " +
                  "http://localhost:8080/reset-password?token=%s").formatted(username,
                token));
    }
}
