/*
 * PowerAuth integration libraries for RESTful API applications, examples and
 * related software components
 *
 * Copyright (C) 2019 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wultra.security.powerauth.webflow.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthAnnotationInterceptor;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthEncryptionArgumentResolver;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthWebArgumentResolver;
import io.getlime.security.powerauth.rest.api.spring.filter.PowerAuthRequestFilter;
import kong.unirest.ObjectMapper;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * MVC configuration for PowerAuth.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 *
 */
@Configuration
public class WebApplicationConfiguration implements WebMvcConfigurer {

    private final com.fasterxml.jackson.databind.ObjectMapper mapper;

    @Autowired
    public WebApplicationConfiguration(com.fasterxml.jackson.databind.ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Register a new @PowerAuth annotation interceptor.
     * @return New annotation interceptor bean.
     */
    @Bean
    public PowerAuthAnnotationInterceptor powerAuthInterceptor() {
        return new PowerAuthAnnotationInterceptor();
    }

    /**
     * Register new method argument resolvers.
     * @return New PowerAuthWebArgumentResolver bean.
     */
    @Bean
    public PowerAuthWebArgumentResolver powerAuthWebArgumentResolver() {
        return new PowerAuthWebArgumentResolver();
    }

    /**
     * Register new method argument resolver for encryption.
     * @return New PowerAuthEncryptionArgumentResolver bean.
     */
    @Bean
    public PowerAuthEncryptionArgumentResolver powerAuthEncryptionArgumentResolver() {
        return new PowerAuthEncryptionArgumentResolver();
    }

    /**
     * Register a new PowerAuthRequestFilter and map it to /* end-point.
     * @return PowerAuthRequestFilter instance.
     */
    @Bean
    public FilterRegistrationBean powerAuthFilterRegistration() {
        FilterRegistrationBean<PowerAuthRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new PowerAuthRequestFilter());
        registrationBean.setMatchAfter(true);
        return registrationBean;
    }

    /**
     * Add method argument resolver for PowerAuthApiAuthentication.
     * @param argumentResolvers List of argument resolvers.
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(powerAuthWebArgumentResolver());
        argumentResolvers.add(powerAuthEncryptionArgumentResolver());
    }

    /**
     * Add annotation interceptor.
     * @param registry Registry of annotation interceptors.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(powerAuthInterceptor());
    }

    @PostConstruct
    public void postConstruct() {
        // Configure Unirest properties
        Unirest.config()
                .setObjectMapper(new ObjectMapper() {

                    public String writeValue(Object value) {
                        try {
                            return mapper.writeValueAsString(value);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    public <T> T readValue(String value, Class<T> valueType) {
                        try {
                            return mapper.readValue(value, valueType);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

}
