package com.pos.infraestructura.dto;

import jakarta.validation.constraints.Positive;

/**
 * Request body para la operación de actualizar la cantidad de un ítem en el carrito.
 * Utilizado en {@code PUT /api/carrito/items/{productoId}}.
 *
 * <p>Requerimientos: 6.2, 5.1</p>
 */
public class ActualizarItemRequest {

    /** Nueva cantidad de unidades. Debe ser un entero positivo (> 0). */
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    // -------------------------------------------------------------------------
    // Constructor sin argumentos
    // -------------------------------------------------------------------------

    /** Constructor sin argumentos requerido para deserialización JSON. */
    public ActualizarItemRequest() {
    }

    // -------------------------------------------------------------------------
    // Getter
    // -------------------------------------------------------------------------

    public Integer getCantidad() {
        return cantidad;
    }

    // -------------------------------------------------------------------------
    // Setter
    // -------------------------------------------------------------------------

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
