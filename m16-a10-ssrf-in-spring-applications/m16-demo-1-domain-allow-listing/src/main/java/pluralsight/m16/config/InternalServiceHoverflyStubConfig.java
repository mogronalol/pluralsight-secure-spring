package pluralsight.m16.config;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newExactMatcher;
import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newRegexMatcher;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;

@Configuration
public class InternalServiceHoverflyStubConfig {

    @Bean(destroyMethod = "close")
    @Order(Integer.MIN_VALUE)
    public Hoverfly hoverfly() {
        final Hoverfly hoverfly =
                new Hoverfly(HoverflyMode.SPY);

        hoverfly.start();

        hoverfly.simulate(
                dsl(service("http://internal.com")
                                .get(newExactMatcher("/secret"))
                                .willReturn(success("""
                                        {
                                            "key": "stripe",
                                            "value": "ac183a68-8939-4aa1-8f68-c5c1129d72e9"
                                        }
                                        """, "application/json")),
                        service("http://other-pluralsight.com")
                                .anyMethod(newRegexMatcher(".*"))
                                .anyQueryParams()
                                .willReturn(success()),
                        service("http://www.pluralsight.com")
                                .anyMethod(newRegexMatcher(".*"))
                                .anyQueryParams()
                                .willReturn(success())));

        return hoverfly;
    }
}
