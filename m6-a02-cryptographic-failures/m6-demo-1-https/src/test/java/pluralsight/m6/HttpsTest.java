package pluralsight.m6;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class HttpsTest {

    @Autowired
    RestClient.Builder builder;

    @Autowired
    RestClientSsl restClientSsl;

    private RestClient restClient;

    @BeforeEach
    public void buildRestClient() {
        restClient = builder
                .apply(restClientSsl.fromBundle("application"))
                .build();
    }

    @Test
    public void testHttpToHttpsRedirect() {

        final ResponseEntity<Void> response = restClient
                .get()
                .uri("http://localhost:8080")
                .retrieve()
                .toBodilessEntity();

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FOUND);

        assertThat(response.getHeaders())
                .containsEntry("Location", List.of("https://localhost:8443/"));
    }

    @Test
    public void testHttpsPortWithHttp() {
        final HttpClientErrorException exception =
                (HttpClientErrorException) catchThrowable(() -> restClient.get()
                        .uri("http://localhost:8443")
                        .retrieve()
                        .toBodilessEntity());

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testStrictTransportSecurityForHttps() {
        final ResponseEntity<Void> responseEntity = restClient.get()
                .uri("https://localhost:8443")
                .retrieve()
                .toBodilessEntity();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders())
                .containsEntry("Strict-Transport-Security", List.of("max-age=31536000 ; includeSubDomains"));
    }
}

