package com.pos.dominio.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entidad de dominio que representa un ítem dentro del carrito de compras.
 * Asocia un Producto con una cantidad y calcula el subtotal correspondiente.
 * Pertenece a la capa de dominio (arquitectura hexagonal).
 */
public class ItemCarrito {

    private Producto producto;
    private Integer cantidad;
    private BigDecimal subtotal;

    /**
     * Constructor con todos los campos.
     * El subtotal se calcula automáticamente como precio × cantidad (escala 2, HALF_UP).
     *
     * @param producto Producto asociado al ítem.
     * @param cantidad Cantidad de unidades del producto.
     * @param subtotal Subtotal precalculado (precio × cantidad).
     */
    public ItemCarrito(Producto producto, Integer cantidad, BigDecimal subtotal) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    /**
     * Constructor de conveniencia que calcula el subtotal automáticamente
     * a partir del precio del producto y la cantidad indicada.
     *
     * @param producto Producto asociado al ítem.
     * @param cantidad Cantidad de unidades del producto.
     */
    public ItemCarrito(Producto producto, Integer cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = calcularSubtotal(producto.getPrecio(), cantidad);
    }

    /**
     * Calcula el subtotal como precio × cantidad con escala 2 y redondeo HALF_UP.
     *
     * @param precio   Precio unitario del producto.
     * @param cantidad Cantidad de unidades.
     * @return Subtotal con escala 2.
     */
    public static BigDecimal calcularSubtotal(BigDecimal precio, int cantidad) {
        return precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public Producto getProducto() {
        return producto;
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

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "ItemCarrito{" +
                "producto=" + producto +
                ", cantidad=" + cantidad +
                ", subtotal=" + subtotal +
                '}';
    }
}
