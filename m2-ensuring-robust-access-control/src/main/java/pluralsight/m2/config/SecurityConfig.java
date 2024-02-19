package pluralsight.m2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import pluralsight.m2.repository.TestDataFactory;
import pluralsight.m2.security.BankingPermissionEvaluator;
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
                                .requestMatchers("/images/**").permitAll()
                                .requestMatchers("/admin/**").hasAnyRole(Roles.CUSTOMER_SERVICE.name(), Roles.CUSTOMER_SERVICE_MANAGER.name())
                                .requestMatchers("/api/**").hasAnyRole(Roles.FRAUD_ANALYST.name())
                                .anyRequest().hasRole(Roles.CUSTOMER.name())
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
    public MethodSecurityExpressionHandler expressionHandler(BankingPermissionEvaluator bankingPermissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(bankingPermissionEvaluator);
        return handler;
    }
}

