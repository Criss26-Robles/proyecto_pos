package com.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del backend del Sistema POS.
 *
 * <p>Punto de entrada de la aplicación Spring Boot. Arranca el contexto de Spring
 * y el servidor embebido Tomcat en el puerto configurado (por defecto 8080).</p>
 */
@SpringBootApplication
public class PosBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PosBackendApplication.class, args);
    }
}
