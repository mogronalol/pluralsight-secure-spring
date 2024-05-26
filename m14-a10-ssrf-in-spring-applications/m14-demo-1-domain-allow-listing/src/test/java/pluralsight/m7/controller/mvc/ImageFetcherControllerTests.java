package pluralsight.m7.controller.mvc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageFetcherControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private static Stream<Object[]> arguments() {
        return Stream.of(
                new Object[]{"http://pluralsight.com", true},
                new Object[]{"http://other-pluralsight.com", true},
                new Object[]{"http://pluralsight.com/path", true},
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

    @ParameterizedTest
    @MethodSource("arguments")
    @WithMockUser
    public void shouldPerformRequest(String url, boolean allowed) throws Exception {

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
}
