package pluralsight.m12.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MfaClient {
    public void sendOtpEmail(final String username, final String token) {
        log.info(("Sending OTP email to %s with token " +
                  "%s").formatted(username,
                token));
    }
}
