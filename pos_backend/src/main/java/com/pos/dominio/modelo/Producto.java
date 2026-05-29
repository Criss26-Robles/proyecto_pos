package com.pos.dominio.modelo;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad de dominio que representa un producto disponible en el catálogo del POS.
 * Pertenece a la capa de dominio (arquitectura hexagonal).
 */
public class Producto {

    private UUID id;
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;

    /**
     * Constructor con todos los campos.
     *
     * @param id     Identificador único del producto.
     * @param codigo Código alfanumérico del producto.
     * @param nombre Nombre descriptivo del producto.
     * @param precio Precio unitario del producto.
     * @param stock  Cantidad disponible en inventario.
     */
    public Producto(UUID id, String codigo, String nombre, BigDecimal precio, Integer stock) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Integer getStock() {
        return stock;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setId(UUID id) {
        this.id = id;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                '}';
    }
}
