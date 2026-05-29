package com.pos.dominio.puerto;

import com.pos.dominio.modelo.Venta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (arquitectura hexagonal) para la persistencia de ventas.
 *
 * <p>Define el contrato que cualquier adaptador de salida debe cumplir para registrar
 * y consultar el historial de ventas. La capa de dominio depende únicamente de esta
 * interfaz, nunca de la implementación concreta.</p>
 *
 * <p>Valida: Requerimientos 3.9, 4.3</p>
 */
public interface VentaRepositorioPuerto {

    /**
     * Persiste una nueva venta y la retorna con su estado final (p. ej. con ID asignado).
     *
     * @param venta instancia de la venta a guardar; no debe ser {@code null}.
     * @return la {@link Venta} guardada, incluyendo el ID generado si aplica.
     */
    Venta guardar(Venta venta);

    /**
     * Retorna el historial completo de ventas registradas.
     *
     * @return lista de ventas; vacía si no se ha realizado ninguna.
     */
    List<Venta> findAll();

    /**
     * Busca una venta por su identificador único.
     *
     * @param id identificador UUID de la venta.
     * @return {@link Optional} con la venta si existe, o vacío si no se encuentra.
     */
    Optional<Venta> findById(UUID id);
}
