package com.duoc.transacciones_service.exception;

public class TransaccionNotFoundException extends RuntimeException {

    public TransaccionNotFoundException(String mensaje) {
        super(mensaje);
    }
}
