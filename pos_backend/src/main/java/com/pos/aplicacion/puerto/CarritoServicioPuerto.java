package com.pos.aplicacion.puerto;

import com.pos.infraestructura.dto.CarritoDTO;

import java.util.UUID;

/**
 * Puerto de entrada (driving port) para las operaciones sobre el carrito de compras.
 * Define el contrato que los adaptadores de entrada (controladores REST) deben
 * invocar para gestionar el carrito activo de la sesión de venta.
 *
 * <p>Pertenece a la capa de aplicación en la arquitectura hexagonal (Ports &amp; Adapters).
 * Las implementaciones concretas residen en {@code com.pos.aplicacion.servicio}.</p>
 *
 * <p>Requerimientos: 4.2</p>
 */
public interface CarritoServicioPuerto {

    /**
     * Retorna el estado actual del carrito de compras.
     *
     * @return {@link CarritoDTO} con los ítems y el total actuales; nunca {@code null}.
     */
    CarritoDTO obtenerCarrito();

    /**
     * Agrega un producto al carrito con la cantidad indicada.
     * Si el producto ya existe en el carrito, incrementa su cantidad.
     *
     * @param productoId identificador UUID del producto a agregar.
     * @param cantidad   número de unidades a agregar (debe ser &gt; 0).
     * @return {@link CarritoDTO} actualizado tras la operación.
     * @throws com.pos.infraestructura.excepcion.ProductoNoEncontradoException
     *         si no existe ningún producto con ese ID.
     * @throws com.pos.infraestructura.excepcion.CantidadInvalidaException
     *         si la cantidad es menor o igual a cero.
     */
    CarritoDTO agregarItem(UUID productoId, int cantidad);

    /**
     * Actualiza la cantidad de un producto ya presente en el carrito.
     *
     * @param productoId identificador UUID del producto a actualizar.
     * @param cantidad   nueva cantidad de unidades (debe ser &gt; 0).
     * @return {@link CarritoDTO} actualizado tras la operación.
     * @throws com.pos.infraestructura.excepcion.ProductoNoEncontradoException
     *         si el producto no se encuentra en el carrito.
     * @throws com.pos.infraestructura.excepcion.CantidadInvalidaException
     *         si la cantidad es menor o igual a cero.
     */
    CarritoDTO actualizarItem(UUID productoId, int cantidad);

    /**
     * Elimina un producto del carrito.
     *
     * @param productoId identificador UUID del producto a eliminar.
     * @return {@link CarritoDTO} actualizado tras la operación.
     * @throws com.pos.infraestructura.excepcion.ProductoNoEncontradoException
     *         si el producto no se encuentra en el carrito.
     */
    CarritoDTO eliminarItem(UUID productoId);

    /**
     * Vacía completamente el carrito, eliminando todos los ítems y poniendo el total a cero.
     *
     * @return {@link CarritoDTO} vacío con total {@code 0.00}.
     */
    CarritoDTO vaciarCarrito();
}
