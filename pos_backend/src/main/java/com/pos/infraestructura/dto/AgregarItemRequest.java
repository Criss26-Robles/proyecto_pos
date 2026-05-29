package com.pos.infraestructura.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * Request body para la operación de agregar un ítem al carrito.
 * Utilizado en {@code POST /api/carrito/items}.
 *
 * <p>Requerimientos: 6.2, 5.1</p>
 */
public class AgregarItemRequest {

    /** Identificador UUID del producto a agregar. No puede ser nulo. */
    @NotNull(message = "El productoId es obligatorio")
    private UUID productoId;

    /** Cantidad de unidades a agregar. Debe ser un entero positivo (> 0). */
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    // -------------------------------------------------------------------------
    // Constructor sin argumentos
    // -------------------------------------------------------------------------

    /** Constructor sin argumentos requerido para deserialización JSON. */
    public AgregarItemRequest() {
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public UUID getProductoId() {
        return productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setProductoId(UUID productoId) {
        this.productoId = productoId;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
