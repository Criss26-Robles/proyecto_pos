package com.pos.infraestructura.adaptador.salida;

import com.pos.dominio.modelo.Carrito;
import com.pos.dominio.puerto.CarritoRepositorioPuerto;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de salida (arquitectura hexagonal) que implementa {@link CarritoRepositorioPuerto}
 * manteniendo un único carrito activo en memoria (patrón singleton de estado).
 *
 * <p>Requerimientos: 4.5</p>
 */
@Repository
public class CarritoRepositorioMemoria implements CarritoRepositorioPuerto {

    private Carrito carrito = new Carrito();

    /**
     * Obtiene el carrito activo actual.
     *
     * @return el {@link Carrito} en curso; nunca {@code null}.
     */
    @Override
    public Carrito obtener() {
        return this.carrito;
    }

    /**
     * Persiste el estado actual del carrito reemplazando la instancia en memoria.
     *
     * @param carrito instancia del carrito a guardar; no debe ser {@code null}.
     */
    @Override
    public void guardar(Carrito carrito) {
        this.carrito = carrito;
    }

    /**
     * Vacía el carrito reemplazándolo por una nueva instancia vacía.
     * Se invoca típicamente tras confirmar una venta.
     */
    @Override
    public void vaciar() {
        this.carrito = new Carrito();
    }
}
