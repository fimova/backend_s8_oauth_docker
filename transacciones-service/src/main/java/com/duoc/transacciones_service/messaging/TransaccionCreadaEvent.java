package com.duoc.transacciones_service.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class TransaccionCreadaEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long transaccionId;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
}
