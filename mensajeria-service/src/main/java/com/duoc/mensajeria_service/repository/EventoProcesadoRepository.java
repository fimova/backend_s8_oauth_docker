package com.duoc.mensajeria_service.repository;

import com.duoc.mensajeria_service.entity.EventoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoProcesadoRepository
        extends JpaRepository<EventoProcesado, Long> {

    boolean existsByTransaccionIdAndTipoEventoAndEstado(
            Long transaccionId,
            String tipoEvento,
            String estado);
}
