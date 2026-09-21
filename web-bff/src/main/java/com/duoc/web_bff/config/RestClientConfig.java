package com.duoc.web_bff.config;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// busca el registro en el discovery server mediante eureka
@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(
            RestTemplateBuilder builder,
            SslBundles sslBundles) {

        return builder
                .sslBundle(sslBundles.getBundle("transacciones"))
                .build();
    }
}
