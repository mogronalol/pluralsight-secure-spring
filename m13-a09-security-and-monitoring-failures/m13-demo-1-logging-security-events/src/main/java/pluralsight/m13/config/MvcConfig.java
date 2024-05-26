package pluralsight.m13.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
    }

//    @Bean
//    public FilterRegistrationBean<RequestAndContextLoggingFilter> loggingFilter() {
//        FilterRegistrationBean<RequestAndContextLoggingFilter> registrationBean =
//                new FilterRegistrationBean<>();
//        registrationBean.setFilter(new RequestAndContextLoggingFilter());
//        registrationBean.setOrder(Integer.MIN_VALUE);
//        return registrationBean;
//    }
}