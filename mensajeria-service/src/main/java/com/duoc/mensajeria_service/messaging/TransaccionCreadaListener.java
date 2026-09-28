package com.duoc.mensajeria_service.messaging;

import com.duoc.mensajeria_service.entity.EventoProcesado;
import com.duoc.mensajeria_service.repository.EventoProcesadoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransaccionCreadaListener {

        private final ObjectMapper objectMapper;
        private final EventoProcesadoRepository eventoProcesadoRepository;

        public TransaccionCreadaListener(
                        ObjectMapper objectMapper,
                        EventoProcesadoRepository eventoProcesadoRepository) {

                this.objectMapper = objectMapper;
                this.eventoProcesadoRepository = eventoProcesadoRepository;
        }

        @JmsListener(destination = "transacciones.creadas")
        public void recibirTransaccion(String mensaje) throws Exception {

                TransaccionCreadaEvent event = objectMapper.readValue(
                                mensaje,
                                TransaccionCreadaEvent.class);

                Long transaccionId = event.getTransaccionId();
                String tipoEvento = "TRANSACCION_CREADA";

                System.out.println(
                                "[EVENTO RECIBIDO] Transacción: "
                                                + transaccionId);

                // Verificar idempotencia
                boolean yaProcesado = eventoProcesadoRepository
                                .existsByTransaccionIdAndTipoEventoAndEstado(
                                                transaccionId,
                                                tipoEvento,
                                                "PROCESADO");

                if (yaProcesado) {

                        System.out.println(
                                        "[EVENTO DUPLICADO] Transacción "
                                                        + transaccionId
                                                        + " ya fue procesada. "
                                                        + "Se ignora redelivery.");

                        return;
                }

                System.out.println(
                                "[PROCESAMIENTO] Procesando transacción...");

                try {

                        // Actualmente el registro del evento representa el procesamiento realizado

                        // SOLO PARA PRUEBAS DE REDELIVERY
                        if (event.getTransaccionId().equals(99999L)) {
                                throw new RuntimeException(
                                                "Error intencional para prueba de procesamiento");
                        }

                        EventoProcesado registro = new EventoProcesado();

                        registro.setTransaccionId(transaccionId);
                        registro.setTipoEvento(tipoEvento);
                        registro.setFechaProcesamiento(
                                        LocalDateTime.now());
                        registro.setEstado("PROCESADO");
                        registro.setDetalle(
                                        "Transacción procesada correctamente");

                        eventoProcesadoRepository.save(registro);

                        System.out.println(
                                        "[PROCESAMIENTO EXITOSO] Transacción "
                                                        + transaccionId
                                                        + " procesada correctamente.");

                } catch (Exception e) {

                        EventoProcesado registroError = new EventoProcesado();

                        registroError.setTransaccionId(transaccionId);
                        registroError.setTipoEvento(tipoEvento);
                        registroError.setFechaProcesamiento(
                                        LocalDateTime.now());
                        registroError.setEstado("ERROR");
                        registroError.setDetalle(
                                        "Error al procesar transacción: "
                                                        + e.getMessage());

                        eventoProcesadoRepository.save(registroError);

                        System.out.println(
                                        "[ERROR DE PROCESAMIENTO] Transacción "
                                                        + transaccionId
                                                        + " no pudo ser procesada.");

                        System.out.println(
                                        "Motivo: " + e.getMessage());

                        // permite que JMS considere fallido el procesamiento
                        // y pueda realizar redelivery
                        throw e;
                }
        }
}