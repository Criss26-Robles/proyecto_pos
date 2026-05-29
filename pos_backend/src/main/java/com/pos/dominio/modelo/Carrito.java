package com.pos.dominio.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de dominio que representa el carrito de compras activo.
 * Contiene la colección de ítems seleccionados y el total acumulado.
 * Pertenece a la capa de dominio (arquitectura hexagonal).
 *
 * <p>Requerimientos: 2.1, 2.2, 2.3, 2.5, 4.1</p>
 */
public class Carrito {

    private List<ItemCarrito> items;
    private BigDecimal total;

    /**
     * Constructor sin argumentos. Inicializa el carrito vacío con total en cero.
     */
    public Carrito() {
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }

    // -------------------------------------------------------------------------
    // Métodos de dominio
    // -------------------------------------------------------------------------

    /**
     * Agrega un producto al carrito con la cantidad indicada.
     * Si el producto ya existe en el carrito, incrementa su cantidad y recalcula
     * el subtotal del ítem. Si no existe, crea un nuevo {@link ItemCarrito} y lo
     * añade a la lista. En ambos casos recalcula el total del carrito.
     *
     * @param producto Producto a agregar.
     * @param cantidad Cantidad de unidades a agregar (debe ser positiva).
     */
    public void agregarItem(Producto producto, int cantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(producto.getId())) {
                int nuevaCantidad = item.getCantidad() + cantidad;
                item.setCantidad(nuevaCantidad);
                item.setSubtotal(ItemCarrito.calcularSubtotal(item.getProducto().getPrecio(), nuevaCantidad));
                recalcularTotal();
                return;
            }
        }
        items.add(new ItemCarrito(producto, cantidad));
        recalcularTotal();
    }

    /**
     * Actualiza la cantidad de un ítem existente en el carrito identificado por
     * el UUID del producto. Recalcula el subtotal del ítem y el total del carrito.
     * Si el producto no se encuentra en el carrito, no realiza ninguna acción.
     *
     * @param productoId UUID del producto cuya cantidad se desea actualizar.
     * @param cantidad   Nueva cantidad (debe ser positiva).
     */
    public void actualizarItem(UUID productoId, int cantidad) {
        for (ItemCarrito item : items) {
            if (item.getProducto().getId().equals(productoId)) {
                item.setCantidad(cantidad);
                item.setSubtotal(ItemCarrito.calcularSubtotal(item.getProducto().getPrecio(), cantidad));
                recalcularTotal();
                return;
            }
        }
    }

    /**
     * Elimina del carrito el ítem cuyo producto tenga el UUID indicado.
     * Recalcula el total tras la eliminación.
     * Si el producto no se encuentra, no realiza ninguna acción.
     *
     * @param productoId UUID del producto a eliminar del carrito.
     */
    public void eliminarItem(UUID productoId) {
        items.removeIf(item -> item.getProducto().getId().equals(productoId));
        recalcularTotal();
    }

    /**
     * Vacía completamente el carrito, eliminando todos los ítems y
     * estableciendo el total en {@link BigDecimal#ZERO}.
     */
    public void vaciar() {
        items.clear();
        total = BigDecimal.ZERO;
    }

    /**
     * Recalcula el total del carrito sumando los subtotales de todos los ítems.
     * El resultado se almacena en el campo {@code total}.
     */
    public void recalcularTotal() {
        total = items.stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Indica si el carrito no contiene ningún ítem.
     *
     * @return {@code true} si la lista de ítems está vacía; {@code false} en caso contrario.
     */
    public boolean estaVacio() {
        return items.isEmpty();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public List<ItemCarrito> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    // -------------------------------------------------------------------------
    // Setters
    // -------------------------------------------------------------------------

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Carrito{" +
                "items=" + items +
                ", total=" + total +
                '}';
    }
}
