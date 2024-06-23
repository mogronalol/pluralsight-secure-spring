package pluralsight.m9;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ProductionErrorPageTest {
    @Autowired
    private RestClient.Builder builder;

    @LocalServerPort
    private int port;

    @Test
    public void noTestUsers() {

        final HttpServerErrorException httpServerErrorException =
                (HttpServerErrorException) catchThrowable(() -> builder.build()
                        .get()
                        .uri("http://localhost:{port}/error-example", port)
                        .retrieve()
                        .body(String.class));

        assertThat(httpServerErrorException.getStatusCode()).isEqualTo(
                HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(httpServerErrorException.getResponseBodyAsString()).contains(
                "Something went wrong at our end, please try again later");
    }
}

