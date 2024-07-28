package pluralsight.m9;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("qa")
public class QaEnvironmentErrorPageTest {

    @Autowired
    private RestClient.Builder builder;

    @LocalServerPort
    private int port;

    @Test
    public void shouldReturnSecureErrorPage() throws Exception {
        final HttpServerErrorException httpServerErrorException =
                catchThrowableOfType(() -> builder.build()
                                .get()
                                .uri("http://localhost:{port}/error-example", port)
                                .retrieve()
                                .toBodilessEntity(),
                        HttpServerErrorException.class
                );

        assertThat(httpServerErrorException.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(httpServerErrorException.getMessage())
                .contains("java.lang.RuntimeException");
    }
}
