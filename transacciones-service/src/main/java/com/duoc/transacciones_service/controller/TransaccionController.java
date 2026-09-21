package com.duoc.transacciones_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.transacciones_service.dto.TransaccionResponse;
import com.duoc.transacciones_service.entity.Transaccion;
import com.duoc.transacciones_service.service.TransaccionService;
import com.duoc.transacciones_service.dto.TransaccionRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

        private final TransaccionService transaccionService;

        public TransaccionController(
                        TransaccionService transaccionService) {

                this.transaccionService = transaccionService;
        }

        @GetMapping
        public List<TransaccionResponse> obtenerTodas() {

                return transaccionService.obtenerTodas()
                                .stream()
                                .map(this::convertirAResponse)
                                .toList();
        }

        private TransaccionResponse convertirAResponse(
                        Transaccion transaccion) {

                TransaccionResponse response = new TransaccionResponse();

                response.setTransaccionId(
                                transaccion.getTransaccionId());

                response.setFecha(
                                transaccion.getFecha());

                response.setMonto(
                                transaccion.getMonto());

                response.setTipo(
                                transaccion.getTipo());

                response.setAnomalia(
                                transaccion.isAnomalia());

                response.setMotivoAnomalia(
                                transaccion.getMotivoAnomalia());

                return response;
        }

        @GetMapping("/{id}")
        public TransaccionResponse obtenerPorId(@PathVariable Long id) {

                Transaccion transaccion = transaccionService.obtenerPorId(id);

                return convertirAResponse(transaccion);
        }

        @PostMapping
        public ResponseEntity<TransaccionResponse> crear(
                        @Valid @RequestBody TransaccionRequest request) {

                Transaccion transaccion = transaccionService.crear(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(convertirAResponse(transaccion));
        }
}
