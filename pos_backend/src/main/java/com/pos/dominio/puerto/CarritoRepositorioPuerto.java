package com.pos.dominio.puerto;

import com.pos.dominio.modelo.Carrito;

/**
 * Puerto de salida (arquitectura hexagonal) para la persistencia del carrito de compras.
 *
 * <p>Define el contrato que cualquier adaptador de salida debe cumplir para mantener
 * el estado del carrito activo. La capa de dominio depende únicamente de esta interfaz,
 * nunca de la implementación concreta (p. ej. en memoria, base de datos, sesión HTTP).</p>
 *
 * <p>Valida: Requerimientos 2.10, 4.3</p>
 */
public interface CarritoRepositorioPuerto {

    /**
     * Obtiene el carrito activo actual.
     *
     * @return el {@link Carrito} en curso; nunca {@code null} (retorna un carrito vacío
     *         si aún no se ha iniciado ninguno).
     */
    Carrito obtener();

    /**
     * Persiste el estado actual del carrito.
     *
     * @param carrito instancia del carrito a guardar; no debe ser {@code null}.
     */
    void guardar(Carrito carrito);

    /**
     * Elimina todos los ítems del carrito, dejándolo en estado vacío.
     * Se invoca típicamente tras confirmar una venta.
     */
    void vaciar();
}
