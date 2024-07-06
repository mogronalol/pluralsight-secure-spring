package pluralsight.m6.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpServerConfig {

    @Value("${http.port}")
    private int httpPort;

    @Bean
    public TomcatServletWebServerFactory tomcat() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    private Connector httpConnector() {
        final Connector connector = new Connector();
        connector.setScheme("http");
        connector.setPort(httpPort);
        return connector;
    }
}
