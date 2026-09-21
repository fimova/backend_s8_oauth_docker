package com.duoc.transacciones_service.entity;

import com.duoc.transacciones_service.enums.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transacciones_diarias")
@Getter
@Setter
@NoArgsConstructor
public class Transaccion {

    @Id
    @Column(name = "transaccion_id")
    private Long transaccionId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoTransaccion tipo;

    @Column(name = "anomalia", nullable = false)
    private boolean anomalia;

    @Column(name = "motivo_anomalia", length = 200)
    private String motivoAnomalia;
}