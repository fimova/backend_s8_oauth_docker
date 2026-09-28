package com.duoc.transacciones_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.duoc.transacciones_service.entity.Transaccion;
import com.duoc.transacciones_service.repository.TransaccionRepository;
import com.duoc.transacciones_service.dto.TransaccionRequest;
import com.duoc.transacciones_service.exception.TransaccionNotFoundException;
import com.duoc.transacciones_service.messaging.TransaccionPublisher;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final TransaccionPublisher transaccionPublisher;

    public TransaccionService(
            TransaccionRepository transaccionRepository,
            TransaccionPublisher transaccionPublisher) {

        this.transaccionRepository = transaccionRepository;
        this.transaccionPublisher = transaccionPublisher;
    }

    public List<Transaccion> obtenerTodas() {
        return transaccionRepository.findAll();
    }

    public Transaccion obtenerPorId(Long id) {
        return transaccionRepository.findById(id)
                .orElseThrow(() -> new TransaccionNotFoundException(
                        "Transacción no encontrada: " + id));
    }

    public Transaccion crear(TransaccionRequest request) {

        Transaccion transaccion = new Transaccion();

        transaccion.setTransaccionId(
                System.currentTimeMillis());

        transaccion.setFecha(request.getFecha());
        transaccion.setMonto(request.getMonto());
        transaccion.setTipo(request.getTipo());

        transaccion.setAnomalia(false);
        transaccion.setMotivoAnomalia(null);

        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        transaccionPublisher.publicarTransaccionCreada(
                transaccionGuardada);

        return transaccionGuardada;
    }
}