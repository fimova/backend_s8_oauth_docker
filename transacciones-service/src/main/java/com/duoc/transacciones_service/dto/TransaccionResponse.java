package com.duoc.transacciones_service.dto;
import com.duoc.transacciones_service.enums.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class TransaccionResponse {

    private Long transaccionId;
    private LocalDate fecha;
    private BigDecimal monto;
    private TipoTransaccion tipo;
    private boolean anomalia;
    private String motivoAnomalia;
    
}
