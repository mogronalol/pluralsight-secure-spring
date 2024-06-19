package pluralsight.m13.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pluralsight.m13.security.RequestAndContextLoggingFilter;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<RequestAndContextLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestAndContextLoggingFilter> registrationBean =
                new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestAndContextLoggingFilter());
        registrationBean.setOrder(Integer.MIN_VALUE);
        return registrationBean;
    }
}