package com.duoc.transacciones_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.duoc.transacciones_service.enums.TipoTransaccion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransaccionRequest {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor que cero")
    private BigDecimal monto;

    @NotNull(message = "El tipo de transacción es obligatorio")
    private TipoTransaccion tipo;
}
