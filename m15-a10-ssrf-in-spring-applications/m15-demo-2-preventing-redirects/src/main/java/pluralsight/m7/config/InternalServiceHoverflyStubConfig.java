package pluralsight.m7.config;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newExactMatcher;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.response;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;

@Configuration
public class InternalServiceHoverflyStubConfig {

    @Bean(destroyMethod = "close")
    public Hoverfly hoverfly() {
        final Hoverfly hoverfly =
                new Hoverfly(HoverflyMode.SPY);

        hoverfly.start();

        hoverfly.simulate(
                dsl(service("http://www.pluralsight.com")
                        .get(newExactMatcher("/redirect-hack"))
                        .willReturn(response()
                                .status(302)
                                .header("Location", "http://internal.com/hacked"))
                ),
                dsl(service("http://internal.com")
                        .get(newExactMatcher("/hacked"))
                        .willReturn(success("hacked", "text/plain; charset=UTF-8"))
                ));

        return hoverfly;
    }
}
