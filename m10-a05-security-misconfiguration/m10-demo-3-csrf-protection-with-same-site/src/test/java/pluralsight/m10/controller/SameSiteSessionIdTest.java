package pluralsight.m10.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SameSiteSessionIdTest {

    @Autowired
    private RestClient.Builder builder;

    @LocalServerPort
    private int port;

    @Test
    public void jsessionIdShouldHaveSameSiteStrict() {
        final ResponseEntity<Void> responseEntity = builder.build()
                .post()
                .uri("http://localhost:{port}/login", port)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(new LinkedMultiValueMap<>(Map.of(
                        "username", List.of("jill"),
                        "password", List.of("password")
                )))
                .retrieve()
                .toBodilessEntity();

        final List<String> setCookie = responseEntity.getHeaders().get("Set-Cookie");

        assertThat(setCookie)
                .first()
                .asString()
                .startsWith("JSESSIONID=")
                .contains("SameSite=Strict");
    }
}
