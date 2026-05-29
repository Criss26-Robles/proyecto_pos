package com.pos.infraestructura.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de la aplicación.
 * Habilita CORS para el frontend en http://localhost:3000.
 *
 * <p>Requerimientos: 5.2</p>
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    /**
     * Configura las reglas CORS para todos los endpoints bajo /api/**.
     *
     * @param registry registro de mappings CORS de Spring MVC.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
