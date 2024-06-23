package pluralsight.m12.config;

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
import pluralsight.m12.security.FailedLoginAttemptHandler;
import pluralsight.m12.security.OptRedirectAuthenticationSuccessHandler;
import pluralsight.m12.security.RedirectPartialLoginFilter;
import pluralsight.m12.security.Roles;

import java.time.Clock;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String[] PUBLIC_URL_MATCHERS = {
            "/images/**",
            "/favicon.ico",
            "/account-registration",
            "/error",
            "/reset-password",
            "/reset-password/initiate",
            "/login"};

    @Autowired
    private FailedLoginAttemptHandler failedLoginAttemptHandler;

    @Autowired
    private RedirectPartialLoginFilter redirectPartialLoginFilter;

    @Autowired
    private OptRedirectAuthenticationSuccessHandler optRedirectAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers(PUBLIC_URL_MATCHERS)
                                .permitAll()
                                .requestMatchers("/login/otp")
                                .hasRole(Roles.PARTIAL_LOGIN_PENDING_OTP.name())
                                .requestMatchers("/**").authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .failureHandler(failedLoginAttemptHandler)
                        .successHandler(optRedirectAuthenticationSuccessHandler)
                        .permitAll()
                )
                .addFilterBefore(redirectPartialLoginFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    @Bean
    public PasswordEncoder delegatingPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

