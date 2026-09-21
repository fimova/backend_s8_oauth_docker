package com.duoc.transacciones_service.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );

        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generarAccessToken(UserDetails userDetails) {

        return generarToken(
                userDetails,
                "access",
                accessTokenExpiration
        );
    }

    public String generarRefreshToken(UserDetails userDetails) {

        return generarToken(
                userDetails,
                "refresh",
                refreshTokenExpiration
        );
    }

    private String generarToken(
            UserDetails userDetails,
            String tipo,
            long expiracion
    ) {

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(
                        "roles",
                        userDetails.getAuthorities()
                                .stream()
                                .map(authority ->
                                        authority.getAuthority())
                                .toList()
                )
                .claim("type", tipo)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expiracion
                        )
                )
                .signWith(key)
                .compact();
    }

    public String obtenerUsername(String token) {

        return obtenerClaims(token)
                .getSubject();
    }

    public boolean esTokenValido(
            String token,
            UserDetails userDetails
    ) {

        String username = obtenerUsername(token);

        return username.equals(userDetails.getUsername())
                && !obtenerClaims(token)
                        .getExpiration()
                        .before(new Date());
    }

    public String obtenerTipo(String token) {

        return obtenerClaims(token)
                .get("type", String.class);
    }

    public void imprimirClaims(String token) {

        Claims claims = obtenerClaims(token);

        System.out.println("===== JWT CLAIMS =====");
        System.out.println("Subject: " + claims.getSubject());
        System.out.println("Roles: " + claims.get("roles"));
        System.out.println("Type: " + claims.get("type"));
        System.out.println("Issued At: " + claims.getIssuedAt());
        System.out.println("Expiration: " + claims.getExpiration());
        System.out.println("======================");
    }

    private Claims obtenerClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
