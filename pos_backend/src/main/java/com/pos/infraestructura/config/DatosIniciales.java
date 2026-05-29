package com.pos.infraestructura.config;

import com.pos.dominio.modelo.Producto;
import com.pos.dominio.puerto.ProductoRepositorioPuerto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Componente de configuración que carga el catálogo inicial de productos al arrancar
 * la aplicación. Implementa {@link CommandLineRunner} para ejecutarse automáticamente
 * después de que el contexto de Spring esté completamente inicializado.
 *
 * <p>Requerimientos: 1.4</p>
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final ProductoRepositorioPuerto productoRepositorio;

    /**
     * Constructor con inyección de dependencia del puerto de repositorio de productos.
     *
     * @param productoRepositorio puerto de salida para persistir productos.
     */
    public DatosIniciales(ProductoRepositorioPuerto productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    /**
     * Inserta 10 productos de ejemplo en el catálogo al iniciar la aplicación.
     *
     * @param args argumentos de línea de comandos (no utilizados).
     */
    @Override
    public void run(String... args) {
        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P001", "Coca-Cola 600ml",
                new BigDecimal("15.00"), 100));

        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P002", "Pepsi 600ml",
                new BigDecimal("14.00"), 80));

        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P003", "Agua Mineral 500ml",
                new BigDecimal("8.00"), 200));

        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P004", "Jugo de Naranja 1L",
                new BigDecimal("22.00"), 50));

        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P005", "Leche Entera 1L",
                new BigDecimal("18.00"), 60));

        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P006", "Pan Integral",
                new BigDecimal("25.00"), 40));

        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P007", "Galletas Oreo",
                new BigDecimal("12.00"), 120));

        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P008", "Chocolate Snickers",
                new BigDecimal("20.00"), 90));

        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P009", "Café Instantáneo 200g",
                new BigDecimal("45.00"), 30));

        productoRepositorio.guardar(new Producto(
                UUID.randomUUID(), "P010", "Cereal Corn Flakes 500g",
                new BigDecimal("55.00"), 25));
    }
}
