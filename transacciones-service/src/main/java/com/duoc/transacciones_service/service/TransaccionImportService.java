package com.duoc.transacciones_service.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.duoc.transacciones_service.entity.Transaccion;
import com.duoc.transacciones_service.enums.TipoTransaccion;
import com.duoc.transacciones_service.repository.TransaccionRepository;

@Service
public class TransaccionImportService {

    private final TransaccionRepository transaccionRepository;

    public TransaccionImportService(
            TransaccionRepository transaccionRepository
    ) {
        this.transaccionRepository = transaccionRepository;
    }

    public void importarTransacciones() {

        ClassPathResource recurso = new ClassPathResource("transacciones.csv");

        int registrosLeidos = 0;
        int registrosValidos = 0;
        int registrosDescartados = 0;
        int registrosConAnomalia = 0;
        int registrosDuplicados = 0;

        try (
            InputStream inputStream = recurso.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            inputStream,
                            StandardCharsets.UTF_8
                    )
            )
        ) {

            // Saltar encabezado
            reader.readLine();

            String linea;

            while ((linea = reader.readLine()) != null) {

            registrosLeidos++;

            try {

                Transaccion transaccion = procesarLinea(linea);

                if (transaccionRepository.existsById(
                        transaccion.getTransaccionId())) {

                    registrosDuplicados++;

                    System.out.println(
                        "Registro duplicado. ID: "
                        + transaccion.getTransaccionId()
                    );

                    continue;
                }

                transaccionRepository.save(transaccion);

                registrosValidos++;

                if (transaccion.isAnomalia()) {
                    registrosConAnomalia++;
                }

            } catch (IllegalArgumentException e) {

                registrosDescartados++;

                System.out.println(
                    "Registro descartado: "
                    + linea
                    + " | Motivo: "
                    + e.getMessage()
                );
            }
        }

        } catch (IOException e) {

            throw new RuntimeException(
                "No fue posible leer el archivo transacciones.csv",
                e
            );
        }

        System.out.println("======================================");
        System.out.println("Importación de transacciones finalizada");
        System.out.println("Registros leídos: " + registrosLeidos);
        System.out.println("Registros persistidos: " + registrosValidos);
        System.out.println("Registros descartados: " + registrosDescartados);
        System.out.println("Registros duplicados: " + registrosDuplicados);
        System.out.println("Registros con anomalía: " + registrosConAnomalia);
        System.out.println("======================================");
    }

    private Transaccion procesarLinea(String linea) {

        String[] campos = linea.split(",", -1);

        if (campos.length != 4) {
            throw new IllegalArgumentException(
                "La línea no contiene exactamente 4 campos."
            );
        }

        Long transaccionId = convertirId(campos[0]);
        LocalDate fecha = convertirFecha(campos[1]);
        BigDecimal monto = convertirMonto(campos[2]);
        TipoTransaccion tipo = convertirTipo(campos[3]);

        Transaccion transaccion = new Transaccion();

        transaccion.setTransaccionId(transaccionId);
        transaccion.setFecha(fecha);
        transaccion.setMonto(monto);
        transaccion.setTipo(tipo);

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {

            transaccion.setAnomalia(true);

            transaccion.setMotivoAnomalia(
                "El monto debe ser mayor a cero."
            );

        } else {

            transaccion.setAnomalia(false);
            transaccion.setMotivoAnomalia(null);
        }

        return transaccion;
    }

    private Long convertirId(String valor) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                "El ID es obligatorio."
            );
        }

        try {

            Long id = Long.parseLong(valor.trim());

            if (id <= 0) {
                throw new IllegalArgumentException(
                    "El ID debe ser mayor que cero."
                );
            }

            return id;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                "El ID no tiene un formato válido."
            );
        }
    }

    private LocalDate convertirFecha(String valor) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                "La fecha es obligatoria."
            );
        }

        String fecha = valor.trim();

        DateTimeFormatter[] formatos = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        };

        for (DateTimeFormatter formato : formatos) {

            try {
                return LocalDate.parse(fecha, formato);

            } catch (DateTimeParseException e) {
                // Intenta con el siguiente formato
            }
        }

        throw new IllegalArgumentException(
            "Formato de fecha no válido."
        );
    }

    private BigDecimal convertirMonto(String valor) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                "El monto es obligatorio."
            );
        }

        try {

            return new BigDecimal(valor.trim());

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                "El monto no tiene un formato válido."
            );
        }
    }

    private TipoTransaccion convertirTipo(String valor) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                "El tipo de transacción es obligatorio."
            );
        }

        try {

            return TipoTransaccion.valueOf(
                valor.trim().toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                "El tipo de transacción debe ser credito o debito."
            );
        }
    }
}