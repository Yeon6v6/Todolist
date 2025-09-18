package com.project.global.config;

import com.project.global.http.filter.ApiPathFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    @Bean
    public FilterRegistrationBean callApiPathfilterRegistrationBean() {
        FilterRegistrationBean<ApiPathFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiPathFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(Integer.MAX_VALUE);
        return registrationBean;
    }
}