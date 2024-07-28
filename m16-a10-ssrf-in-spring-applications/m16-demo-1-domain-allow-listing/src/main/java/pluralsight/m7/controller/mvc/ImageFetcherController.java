package pluralsight.m7.controller.mvc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ImageFetcherController {

    private static final List<String> ALLOWED_DOMAINS = List.of(
            "www.pluralsight.com",
            "other-pluralsight.com"
    );

    private final RestClient restClient;

    @GetMapping("/")
    private String imageConversion() {
        return "image-fetch.html";
    }

    @GetMapping("/fetch-image")
    public ResponseEntity<byte[]> fetchImage(@RequestParam("url") String urlString) {

        final URI uri = URI.create(urlString);

        if (ALLOWED_DOMAINS.stream().noneMatch(u -> uri.getHost().startsWith(u))) {
            return ResponseEntity.badRequest().build();
        }

        final ResponseEntity<byte[]> responseEntity =
                restClient.get()
                        .uri(uri)
                        .retrieve()
                        .toEntity(byte[].class);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);
        headers.setContentType(responseEntity.getHeaders().getContentType());

        return new ResponseEntity<>(responseEntity.getBody(), headers, HttpStatus.OK);
    }
}