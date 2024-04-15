package pluralsight.m2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import pluralsight.m2.repository.TestDataFactory;
import pluralsight.m2.security.Authorities;
import pluralsight.m2.security.Roles;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests ->
                        requests


                                .requestMatchers("/admin/accounts")
                                .hasAuthority(Authorities.VIEW_ACCOUNTS.name())

                                .requestMatchers("/admin/transfer")
                                .hasAuthority(Authorities.TRANSFERS.name())

                                .requestMatchers("/my-accounts", "/accounts/*/transactions")
                                .hasRole(Roles.CUSTOMER.name())

                                .requestMatchers("/").authenticated()

                                .requestMatchers("/images/**", "/favicon.ico")
                                .permitAll()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(TestDataFactory.USERS);
    }

}

