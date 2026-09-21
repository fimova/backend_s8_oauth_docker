package com.duoc.web_bff.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            String username =
                    jwtService.obtenerUsername(token);

            String tipo =
                    jwtService.obtenerTipo(token);

            if (username != null
                    && "access".equals(tipo)
                    && SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                Claims claims =
                        jwtService.obtenerClaims(token);

                @SuppressWarnings("unchecked")
                List<String> roles =
                        claims.get("roles", List.class);

                List<GrantedAuthority> authorities =
                        roles == null
                                ? List.of()
                                : roles.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .map(authority ->
                                                (GrantedAuthority) authority)
                                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception e) {
            // Token inválido: la petición continuará sin autenticación.
        }

        filterChain.doFilter(request, response);
    }
}
