package com.pos.infraestructura.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO que representa una venta confirmada en las respuestas de la API REST.
 * Contiene el identificador, los ítems vendidos, el total y la marca de tiempo.
 *
 * <p>Requerimientos: 6.1, 5.1</p>
 */
public class VentaDTO {

    private UUID id;
    private List<ItemCarritoDTO> items;
    private BigDecimal total;
    private String fechaHora;

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------

    /** Constructor sin argumentos requerido para serialización/deserialización JSON. */
    public VentaDTO() {
    }

    /**
     * Constructor con todos los campos.
     *
     * @param id        Identificador único de la venta.
     * @param items     Lista de ítems vendidos.
     * @param total     Total de la venta.
     * @param fechaHora Marca de tiempo de la confirmación (formato ISO-8601).
     */
    public VentaDTO(UUID id, List<ItemCarritoDTO> items, BigDecimal total, String fechaHora) {
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

    public List<ItemCarritoDTO> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setId(UUID id) {
        this.id = id;
    }

    public void setItems(List<ItemCarritoDTO> items) {
        this.items = items;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
}
