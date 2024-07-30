package pluralsight.m13.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MfaLoginClient {
    public void sendLoginOtp(String username, String otp) {
        log.info("Sending OTP to {} with token {}", username, otp);
    }
}
