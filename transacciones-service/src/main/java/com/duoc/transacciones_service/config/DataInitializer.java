package com.duoc.transacciones_service.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duoc.transacciones_service.repository.TransaccionRepository;
import com.duoc.transacciones_service.service.TransaccionImportService;

//clase auxiliar para procesar los datos al iniciar la app, a menos que ya hayan datos en la tabla
@Configuration
public class DataInitializer {

    @Bean
    ApplicationRunner importarDatos(
            TransaccionImportService importService,
            TransaccionRepository transaccionRepository) {

        return args -> {

            if (transaccionRepository.count() > 0) {
                return;
            }

            importService.importarTransacciones();
        };
    }
}
