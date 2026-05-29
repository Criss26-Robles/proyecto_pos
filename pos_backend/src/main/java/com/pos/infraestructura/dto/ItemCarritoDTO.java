package com.pos.infraestructura.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO que representa un ítem dentro del carrito de compras en las respuestas de la API REST.
 * Contiene la información desnormalizada del producto junto con la cantidad y el subtotal.
 *
 * <p>Requerimientos: 6.1, 5.1</p>
 */
public class ItemCarritoDTO {

    private UUID productoId;
    private String nombreProducto;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private BigDecimal subtotal;

    // -------------------------------------------------------------------------
    // Constructores
    // -------------------------------------------------------------------------

    /** Constructor sin argumentos requerido para serialización/deserialización JSON. */
    public ItemCarritoDTO() {
    }

    /**
     * Constructor con todos los campos.
     *
     * @param productoId      Identificador UUID del producto.
     * @param nombreProducto  Nombre del producto.
     * @param precioUnitario  Precio unitario del producto.
     * @param cantidad        Cantidad de unidades en el carrito.
     * @param subtotal        Subtotal calculado (precioUnitario × cantidad).
     */
    public ItemCarritoDTO(UUID productoId, String nombreProducto, BigDecimal precioUnitario,
                          Integer cantidad, BigDecimal subtotal) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public UUID getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setProductoId(UUID productoId) {
        this.productoId = productoId;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
