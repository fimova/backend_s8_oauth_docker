package com.duoc.transacciones_service.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.transacciones_service.dto.LoginRequest;
import com.duoc.transacciones_service.dto.LoginResponse;
import com.duoc.transacciones_service.dto.RefreshTokenRequest;
import com.duoc.transacciones_service.service.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserDetailsService userDetailsService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()));

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String accessToken =
                jwtService.generarAccessToken(userDetails);

        String refreshToken =
                jwtService.generarRefreshToken(userDetails);

        return new LoginResponse(
                accessToken,
                refreshToken);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(
            @RequestBody RefreshTokenRequest request) {

        String refreshToken =
                request.getRefreshToken();

        if (!"refresh".equals(
                jwtService.obtenerTipo(refreshToken))) {

            throw new IllegalArgumentException(
                    "El token enviado no es un refresh token");
        }

        String username =
                jwtService.obtenerUsername(refreshToken);

        UserDetails userDetails =
                userDetailsService
                        .loadUserByUsername(username);

        if (!jwtService.esTokenValido(
                refreshToken,
                userDetails)) {

            throw new IllegalArgumentException(
                    "El refresh token no es válido");
        }

        String newAccessToken =
                jwtService.generarAccessToken(userDetails);

        return new LoginResponse(
                newAccessToken,
                refreshToken);
    }
}
