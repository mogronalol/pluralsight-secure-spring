package pluralsight.m4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import pluralsight.m4.repository.TestDataFactory;
import pluralsight.m4.security.Authorities;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests ->
                        requests

                                .requestMatchers("/admin/accounts", "/admin/accounts/*")
                                .hasAuthority(Authorities.VIEW_ACCOUNTS.name())

                                .requestMatchers("/admin/transfer")
                                .hasAuthority(Authorities.TRANSFERS.name())

                                .requestMatchers("/employees/**")
                                .hasAuthority(Authorities.VIEW_EMPLOYEES.name())

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

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_CUSTOMER_SERVICE > VIEW_ACCOUNTS
                ROLE_CUSTOMER_SERVICE_MANAGER > ROLE_CUSTOMER_SERVICE
                ROLE_CUSTOMER_SERVICE_MANAGER > TRANSFERS
                ROLE_HUMAN_RESOURCES > VIEW_EMPLOYEES
                ROLE_SENIOR_VICE_PRESIDENT > ROLE_CUSTOMER_SERVICE_MANAGER
                ROLE_SENIOR_VICE_PRESIDENT > ROLE_HUMAN_RESOURCES
                """);
    }
}

