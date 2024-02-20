package pluralsight.m2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pluralsight.m2.repository.TestDataFactory;
import pluralsight.m2.security.BankingPermissionEvaluator;
import pluralsight.m2.security.Roles;

import java.util.Arrays;
import java.util.List;

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
                                .requestMatchers("/").hasAnyRole(Arrays.stream(Roles.values()).map(Enum::name).toArray(String[]::new))
                                .anyRequest().hasRole(Roles.CUSTOMER.name())
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration() {
            @Override
            public String checkOrigin(final String origin) {
                return origin;
            }
        };
        configuration.setAllowedMethods(List.of("*")); // Allow all methods
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(true); // Optional: based on your specific needs
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply the configuration to all paths
        return source;
    }
}

