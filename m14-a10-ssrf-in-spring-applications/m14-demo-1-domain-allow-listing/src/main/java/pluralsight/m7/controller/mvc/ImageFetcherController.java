package pluralsight.m7.controller.mvc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ImageFetcherController {

    private static final List<String>
            allowedDomains = Arrays.asList("pluralsight.com", "other-pluralsight.com");

    private final RestTemplate restTemplate;

    @GetMapping("/")
    private String imageConversion() {
        return "image-fetch.html";
    }

    @GetMapping("/fetch-image")
    public ResponseEntity<byte[]> fetchImage(@RequestParam("url") String urlString) {

        final URI uri = URI.create(urlString);

        if (allowedDomains.stream().noneMatch(u -> uri.getHost().startsWith(u))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final ResponseEntity<byte[]> responseEntity =
                restTemplate.getForEntity(uri, byte[].class);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);
        headers.setContentType(responseEntity.getHeaders().getContentType());

        return new ResponseEntity<>(responseEntity.getBody(), headers, HttpStatus.OK);
    }
}