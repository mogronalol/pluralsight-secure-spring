package pluralsight.m3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import pluralsight.m3.repository.TestDataFactory;
import pluralsight.m3.security.Roles;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class SecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers("/admin/transfer")
                                    .hasRole(Roles.CUSTOMER_SERVICE_MANAGER.name())
                                .requestMatchers("/admin/accounts/**")
                                .hasAnyRole(Roles.CUSTOMER_SERVICE_MANAGER.name(), Roles.CUSTOMER_SERVICE.name())
                                .requestMatchers("/my-accounts",
                                        "/accounts/*/transactions").hasRole(Roles.CUSTOMER.name())
                                .requestMatchers("/", "/error").authenticated()
                                .requestMatchers("/images/**", "/favicon.ico").permitAll()
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

