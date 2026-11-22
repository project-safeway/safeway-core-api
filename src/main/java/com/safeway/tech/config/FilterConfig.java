package com.safeway.tech.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new RateLimitFilter());
        registrationBean.addUrlPatterns("/auth/v2/login", "/auth/login");
        registrationBean.setOrder(1);
        registrationBean.setName("rateLimitFilter");

        return registrationBean;
    }

}
