package pluralsight.m14.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.net.HttpURLConnection;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private RestClient.Builder builder;

    @Bean
    public RestClient restClient() {
        return builder
                .requestFactory(new SimpleClientHttpRequestFactory() {
                    @Override
                    protected void prepareConnection(@NotNull final HttpURLConnection connection,
                                                     @NotNull final String httpMethod)
                            throws IOException {
                        super.prepareConnection(connection, httpMethod);
                        connection.setInstanceFollowRedirects(false);
                    }
                })
                .build();
    }
}