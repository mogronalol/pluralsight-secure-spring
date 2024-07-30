package pluralsight.m16.controller.mvc;

import io.specto.hoverfly.junit.core.Hoverfly;
import io.specto.hoverfly.junit.core.HoverflyMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.core.model.RequestFieldMatcher.newRegexMatcher;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.response;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageFetcherControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private Hoverfly hoverfly;

    private static Stream<Object[]> arguments() {
        return Stream.of(
                new Object[]{"http://www.pluralsight.com", true},
                new Object[]{"http://other-pluralsight.com", true},
                new Object[]{"http://www.pluralsight.com/path", true},
                new Object[]{"http://www.pluralsight.com/path", true},
                new Object[]{"http://other-pluralsight.com/path", true},
                new Object[]{"http://subdomain.pluralsight.com", false},
                new Object[]{"http://subdomain.other-pluralsight.com", false},
                new Object[]{"http://pluralsight.net", false},
                new Object[]{"http://other-pluralsight.net", false},
                new Object[]{"http://ppluralsight.com/path", false},
                new Object[]{"http://pother-pluralsight.com/path", false},
                new Object[]{"http://subdomain.pluralsight2.com", false},
                new Object[]{"http://subdomain.other-pluralsight.net", false}
        );
    }

    @BeforeEach
    public void setUp() {
        hoverfly = new Hoverfly(HoverflyMode.SIMULATE);
        hoverfly.start();
    }

    @AfterEach
    public void afterEach() {
        hoverfly.close();
    }

    @ParameterizedTest
    @MethodSource("arguments")
    @WithMockUser
    public void shouldPerformRequest(String url, boolean allowed) throws Exception {

        hoverfly.simulate(
                dsl(service("http://www.pluralsight.com")
                        .anyMethod(newRegexMatcher(".*"))
                        .willReturn(success())),
                dsl(service("http://other-pluralsight.com")
                        .anyMethod(newRegexMatcher(".*"))
                        .willReturn(success())
                ));

        final ResultActions perform = mockMvc.perform(get("/fetch-image")
                .param("url", url));

        if (allowed) {
            perform
                    .andExpect(status().isOk());
        } else {
            perform
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    public void shouldNotFollowRedirects() throws Exception {
        hoverfly.simulate(
                dsl(service("http://www.pluralsight.com")
                        .get("/redirect-hack")
                        .willReturn(response()
                                .status(302)
                                .header("Location", "http://internal.com/hacked"))
                ),
                dsl(service("http://internal.com")
                        .get("/hacked")
                        .willReturn(success("hacked", "text/plain; charset=UTF-8")))
        );

        mockMvc.perform(get("/fetch-image")
                .param("url", "http://www.pluralsight.com/redirect-hack"))
                .andExpect(content().string(not(containsString("hacked"))));
    }
}
