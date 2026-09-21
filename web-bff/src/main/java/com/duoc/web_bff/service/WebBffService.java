package com.duoc.web_bff.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import com.duoc.web_bff.dto.TransaccionResponse;

@Service
public class WebBffService {

    private final RestTemplate restTemplate;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public WebBffService(
            RestTemplate restTemplate,
            CircuitBreakerFactory circuitBreakerFactory) {

        this.restTemplate = restTemplate;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public List<TransaccionResponse> obtenerTransacciones(
            String authorization) {

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("transacciones");

        return circuitBreaker.run(
                () -> realizarSolicitud(authorization),
                throwable -> obtenerTransaccionesFallback(
                        authorization,
                        throwable));
    }

    private List<TransaccionResponse> realizarSolicitud(
            String authorization) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TransaccionResponse[]> response = restTemplate.exchange(
                "https://transacciones-service/api/transacciones",
                HttpMethod.GET,
                entity,
                TransaccionResponse[].class);

        TransaccionResponse[] body = response.getBody();

        return body != null
                ? List.of(body)
                : List.of();
    }

    // si la llamada falla y circuit breaker dice que hay que usar fallback,
    // devuelve una lista vacía
    public List<TransaccionResponse> obtenerTransaccionesFallback(
            String authorization,
            Throwable exception) {

        System.out.println(
                "FALLBACK EJECUTADO: " + exception.getMessage());

        return List.of();
    }
}
