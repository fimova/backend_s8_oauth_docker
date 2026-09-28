package com.duoc.transacciones_service.messaging;

import com.duoc.transacciones_service.entity.Transaccion;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransaccionPublisher {

        private static final String TOPIC = "transacciones.creadas";

        private final JmsTemplate jmsTemplate;
        private final Retry retry;
        private final CircuitBreaker circuitBreaker;

        public TransaccionPublisher(
                        JmsTemplate jmsTemplate,
                        RetryRegistry retryRegistry,
                        CircuitBreakerRegistry circuitBreakerRegistry) {

                this.jmsTemplate = jmsTemplate;

                this.retry = retryRegistry.retry("activemq");

                this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("activemq");

                configurarEventos();
        }

        public void publicarTransaccionCreada(
                        Transaccion transaccion) {

                TransaccionCreadaEvent event = new TransaccionCreadaEvent(
                                transaccion.getTransaccionId(),
                                transaccion.getFecha(),
                                transaccion.getMonto(),
                                transaccion.getTipo().name());

                System.out.println(
                                "[EVENTO] Publicando transacción "
                                                + event.getTransaccionId());

                Runnable publicarConRetry = Retry.decorateRunnable(
                                retry,
                                () -> {

                                        System.out.println(
                                                        "[RETRY] Intento de publicación para transacción "
                                                                        + event.getTransaccionId());

                                        jmsTemplate.convertAndSend(
                                                        TOPIC,
                                                        event);
                                });

                try {

                        circuitBreaker.executeRunnable(
                                        publicarConRetry);

                        System.out.println(
                                        "[EVENTO PUBLICADO] Transacción "
                                                        + event.getTransaccionId()
                                                        + " enviada a ActiveMQ.");

                } catch (Exception e) {

                        System.out.println(
                                        "[ERROR PUBLICANDO EVENTO]");

                        System.out.println(
                                        "No fue posible publicar la transacción "
                                                        + event.getTransaccionId()
                                                        + " después de "
                                                        + retry.getRetryConfig().getMaxAttempts()
                                                        + " intentos.");

                        throw e;
                }
        }

        private void configurarEventos() {

                retry.getEventPublisher()
                                .onRetry(event -> System.out.println(
                                                "[RETRY] Falló intento "
                                                                + event.getNumberOfRetryAttempts()
                                                                + " de publicación."));

                circuitBreaker.getEventPublisher()
                                .onStateTransition(event -> System.out.println(
                                                "[CIRCUIT BREAKER] Estado: "
                                                                + event.getStateTransition()));
        }
}