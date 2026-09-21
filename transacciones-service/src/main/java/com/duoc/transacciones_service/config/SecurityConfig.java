package com.duoc.transacciones_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.duoc.transacciones_service.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsManager users(
            PasswordEncoder passwordEncoder) {

        UserDetails usuarioWeb = User.builder()
                .username("usuarioWeb")
                .password(passwordEncoder.encode("web123"))
                .roles("WEB")
                .build();

        UserDetails usuarioMobile = User.builder()
                .username("usuarioMobile")
                .password(passwordEncoder.encode("mobile123"))
                .roles("MOBILE")
                .build();

        UserDetails usuarioAtm = User.builder()
                .username("usuarioAtm")
                .password(passwordEncoder.encode("atm123"))
                .roles("ATM")
                .build();

        return new InMemoryUserDetailsManager(
                usuarioWeb,
                usuarioMobile,
                usuarioAtm);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/auth/login",
                    "/auth/refresh")
                .permitAll()

                .requestMatchers("/error")
                .permitAll()

                .requestMatchers("/api/transacciones/**")
                .hasRole("WEB")

                .anyRequest()
                .authenticated()
            );

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}