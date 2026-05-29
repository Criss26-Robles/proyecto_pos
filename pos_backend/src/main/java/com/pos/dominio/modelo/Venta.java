package com.pos.dominio.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de dominio que representa una venta confirmada en el sistema POS.
 * Registra los ítems vendidos, el total de la transacción y la marca de tiempo.
 * Pertenece a la capa de dominio (arquitectura hexagonal).
 *
 * Requerimientos: 3.1, 3.8, 4.1
 */
public class Venta {

    private UUID id;
    private List<ItemCarrito> items;
    private BigDecimal total;
    private LocalDateTime fechaHora;

    /**
     * Constructor con todos los campos.
     *
     * @param id        Identificador único de la venta (UUID).
     * @param items     Lista de ítems vendidos (copia del carrito confirmado).
     * @param total     Total de la venta (suma de subtotales de los ítems).
     * @param fechaHora Marca de tiempo del momento en que se confirmó la venta.
     */
    public Venta(UUID id, List<ItemCarrito> items, BigDecimal total, LocalDateTime fechaHora) {
        this.id = id;
        this.items = items;
        this.total = total;
        this.fechaHora = fechaHora;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setId(UUID id) {
        this.id = id;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Venta{" +
                "id=" + id +
                ", items=" + items +
                ", total=" + total +
                ", fechaHora=" + fechaHora +
                '}';
    }
}
