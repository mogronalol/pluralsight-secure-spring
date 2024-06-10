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
import pluralsight.m6.domain.Account;
import pluralsight.m6.repository.AccountRepository;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class MutualTlsTest {

    @Autowired
    RestClient.Builder builder;

    @Autowired
    RestClientSsl restClientSsl;

    @Autowired
    private AccountRepository accountRepository;

    private RestClient restClient;

    @BeforeEach
    public void buildRestClient() {
        restClient = builder
                .apply(restClientSsl.fromBundle("client"))
                .build();

        accountRepository.save(Account.builder()
                    .accountCode("accountCode")
                    .index(0)
                    .username("test")
                    .displayName("test account")
                    .build());
    }

    @Test
    public void testHttpRejected() {

        assertThatThrownBy(() -> restClient
                .get()
                .uri("http://localhost:8080")
                .retrieve()
                .toBodilessEntity())
                .hasCauseInstanceOf(ConnectException.class)
                .hasMessageContaining("Connection refused");
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
    public void testMutualTlsPermitted() {
        final ResponseEntity<Void> responseEntity = restClient.get()
                .uri("https://localhost:8443/accounts/accountCode")
                .retrieve()
                .toBodilessEntity();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testOneWayTlsRejected() {
        assertThatThrownBy(() -> builder
                .apply(restClientSsl.fromBundle("client-no-client-cert"))
                .build()
                .get()
                .uri("https://localhost:8443")
                .retrieve()
                .toBodilessEntity())
                .hasCauseInstanceOf(SSLHandshakeException.class)
                .hasMessageContaining("bad_certificate");
    }
}

