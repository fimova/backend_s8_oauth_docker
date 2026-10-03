package com.duoc.transacciones_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http)
                        throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers("/error")
                                                .permitAll()

                                                .requestMatchers(
                                                                "/actuator/health",
                                                                "/actuator/info")
                                                .permitAll()

                                                .requestMatchers("/api/transacciones/**")
                                                .hasRole("WEB")

                                                .anyRequest()
                                                .authenticated())

                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                new JwtAuthenticationConverter())));

                return http.build();
        }
}