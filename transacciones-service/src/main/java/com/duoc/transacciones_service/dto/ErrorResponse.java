package com.duoc.transacciones_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String mensaje;
    private LocalDateTime timestamp;
}
