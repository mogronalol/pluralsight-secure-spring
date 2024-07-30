package pluralsight.m13.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import pluralsight.m13.security.FailedLoginAttemptHandler;
import pluralsight.m13.security.OtpRedirectAuthenticationSuccessHandler;
import pluralsight.m13.security.RedirectPartialLoginFilter;

import java.time.Clock;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private FailedLoginAttemptHandler failedLoginAttemptHandler;

    @Autowired
    private OtpRedirectAuthenticationSuccessHandler otpRedirectAuthenticationSuccessHandler;

    @Autowired
    private RedirectPartialLoginFilter redirectPartialLoginFilter;

    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers(
                                        "/login/otp"
                                )
                                .hasAuthority(
                                        OtpRedirectAuthenticationSuccessHandler.ROLE_PARTIAL_LOGIN_OTP)
                                .requestMatchers(
                                        "/images/**",
                                        "/favicon.ico",
                                        "/account-registration",
                                        "/error",
                                        "/reset-password/**",
                                        "/login")
                                .permitAll()
                                .requestMatchers("/**").authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .failureHandler(failedLoginAttemptHandler)
                        .successHandler(otpRedirectAuthenticationSuccessHandler)
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll)
                .addFilterBefore(redirectPartialLoginFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder delegatingPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

