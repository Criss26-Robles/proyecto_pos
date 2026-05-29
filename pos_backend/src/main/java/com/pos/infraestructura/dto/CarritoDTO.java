package com.pos.infraestructura.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que representa el estado completo del carrito de compras en las respuestas de la API REST.
 * Contiene la lista de ítems y el total acumulado.
 *
 * <p>Requerimientos: 6.1, 5.1</p>
 */
public class CarritoDTO {

    private List<ItemCarritoDTO> items;
    private BigDecimal total;

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------

    /** Constructor sin argumentos requerido para serialización/deserialización JSON. */
    public CarritoDTO() {
    }

    /**
     * Constructor con todos los campos.
     *
     * @param items Lista de ítems presentes en el carrito.
     * @param total Total acumulado de todos los subtotales.
     */
    public CarritoDTO(List<ItemCarritoDTO> items, BigDecimal total) {
        this.items = items;
        this.total = total;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public List<ItemCarritoDTO> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setItems(List<ItemCarritoDTO> items) {
        this.items = items;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
