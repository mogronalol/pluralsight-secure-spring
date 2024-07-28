package pluralsight.m9;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductionErrorPageTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnSecureErrorPage() throws Exception {
        mockMvc.perform(get("/error-example"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string(containsString("Something went wrong at our end")));
    }
}
