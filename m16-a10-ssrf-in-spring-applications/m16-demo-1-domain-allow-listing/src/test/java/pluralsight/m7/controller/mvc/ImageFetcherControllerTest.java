package pluralsight.m7.controller.mvc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageFetcherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    public static Stream<Object[]> arguments() {
        return Stream.of(
                new Object[] {"http://www.pluralsight.com", true},
                new Object[] {"http://other-pluralsight.com", true},
                new Object[] {"http://www.pluralsight.com/path", true},
                new Object[] {"http://subdomain.pluralsight.com", false},
                new Object[] {"http://subdomain-other.pluralsight.com", false},
                new Object[] {"http://pluralsight.net", false},
                new Object[] {"http://other-pluralsight.net", false},
                new Object[] {"http://ppluralsight.com/path", false},
                new Object[] {"http://pother-pluralsight.com/path", false}
        );
    }

    @ParameterizedTest
    @MethodSource("arguments")
    public void canOnlyPerformRequestToAllowedDomains(String url, boolean allowed)
            throws Exception {

        final ResultActions perform =
                mockMvc.perform(MockMvcRequestBuilders.get("/fetch-image")
                        .param("url", url));

        if (allowed) {
            perform.andExpect(status().isOk());
        } else {
            perform.andExpect(status().isBadRequest());
        }
    }
}
