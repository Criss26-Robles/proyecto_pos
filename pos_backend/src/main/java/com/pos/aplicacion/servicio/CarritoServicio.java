package com.pos.aplicacion.servicio;

import com.pos.aplicacion.puerto.CarritoServicioPuerto;
import com.pos.dominio.modelo.Carrito;
import com.pos.dominio.modelo.ItemCarrito;
import com.pos.dominio.modelo.Producto;
import com.pos.dominio.puerto.CarritoRepositorioPuerto;
import com.pos.dominio.puerto.ProductoRepositorioPuerto;
import com.pos.infraestructura.dto.CarritoDTO;
import com.pos.infraestructura.dto.ItemCarritoDTO;
import com.pos.infraestructura.excepcion.CantidadInvalidaException;
import com.pos.infraestructura.excepcion.ProductoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para la gestión del carrito de compras.
 * Implementa el puerto de entrada {@link CarritoServicioPuerto} y delega
 * la persistencia a los puertos de salida {@link ProductoRepositorioPuerto}
 * y {@link CarritoRepositorioPuerto}.
 *
 * <p>Requerimientos: 2.1–2.10, 4.4, 4.5</p>
 */
@Service
public class CarritoServicio implements CarritoServicioPuerto {

    private final ProductoRepositorioPuerto productoRepositorio;
    private final CarritoRepositorioPuerto carritoRepositorio;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param productoRepositorio puerto de salida para acceder al catálogo de productos.
     * @param carritoRepositorio  puerto de salida para persistir el estado del carrito.
     */
    public CarritoServicio(ProductoRepositorioPuerto productoRepositorio,
                           CarritoRepositorioPuerto carritoRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.carritoRepositorio = carritoRepositorio;
    }

    /**
     * Retorna el estado actual del carrito de compras.
     *
     * @return {@link CarritoDTO} con los ítems y el total actuales.
     */
    @Override
    public CarritoDTO obtenerCarrito() {
        Carrito carrito = carritoRepositorio.obtener();
        return mapearADTO(carrito);
    }

    /**
     * Agrega un producto al carrito con la cantidad indicada.
     * Si el producto ya existe en el carrito, incrementa su cantidad.
     *
     * @param productoId identificador UUID del producto a agregar.
     * @param cantidad   número de unidades a agregar (debe ser &gt; 0).
     * @return {@link CarritoDTO} actualizado tras la operación.
     * @throws CantidadInvalidaException     si la cantidad es menor o igual a cero.
     * @throws ProductoNoEncontradoException si no existe ningún producto con ese ID.
     */
    @Override
    public CarritoDTO agregarItem(UUID productoId, int cantidad) {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException("La cantidad debe ser mayor que cero");
        }
        Producto producto = productoRepositorio.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "Producto no encontrado con id: " + productoId));
        Carrito carrito = carritoRepositorio.obtener();
        carrito.agregarItem(producto, cantidad);
        carritoRepositorio.guardar(carrito);
        return mapearADTO(carrito);
    }

    /**
     * Actualiza la cantidad de un producto ya presente en el carrito.
     *
     * @param productoId identificador UUID del producto a actualizar.
     * @param cantidad   nueva cantidad de unidades (debe ser &gt; 0).
     * @return {@link CarritoDTO} actualizado tras la operación.
     * @throws CantidadInvalidaException si la cantidad es menor o igual a cero.
     */
    @Override
    public CarritoDTO actualizarItem(UUID productoId, int cantidad) {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException("La cantidad debe ser mayor que cero");
        }
        Carrito carrito = carritoRepositorio.obtener();
        carrito.actualizarItem(productoId, cantidad);
        carritoRepositorio.guardar(carrito);
        return mapearADTO(carrito);
    }

    /**
     * Elimina un producto del carrito.
     *
     * @param productoId identificador UUID del producto a eliminar.
     * @return {@link CarritoDTO} actualizado tras la operación.
     */
    @Override
    public CarritoDTO eliminarItem(UUID productoId) {
        Carrito carrito = carritoRepositorio.obtener();
        carrito.eliminarItem(productoId);
        carritoRepositorio.guardar(carrito);
        return mapearADTO(carrito);
    }

    /**
     * Vacía completamente el carrito, eliminando todos los ítems y poniendo el total a cero.
     *
     * @return {@link CarritoDTO} vacío con total {@code 0.00}.
     */
    @Override
    public CarritoDTO vaciarCarrito() {
        carritoRepositorio.vaciar();
        Carrito carrito = carritoRepositorio.obtener();
        return mapearADTO(carrito);
    }

    // -------------------------------------------------------------------------
    // Métodos privados de mapeo
    // -------------------------------------------------------------------------

    /**
     * Convierte una entidad {@link Carrito} en su representación DTO.
     *
     * @param carrito entidad de dominio a convertir.
     * @return {@link CarritoDTO} con los ítems mapeados y el total.
     */
    private CarritoDTO mapearADTO(Carrito carrito) {
        List<ItemCarritoDTO> itemsDTO = carrito.getItems()
                .stream()
                .map(this::mapearItemADTO)
                .collect(Collectors.toList());
        return new CarritoDTO(itemsDTO, carrito.getTotal());
    }

    /**
     * Convierte un {@link ItemCarrito} en su representación DTO.
     *
     * @param item ítem del carrito a convertir.
     * @return {@link ItemCarritoDTO} con los datos del ítem.
     */
    private ItemCarritoDTO mapearItemADTO(ItemCarrito item) {
        return new ItemCarritoDTO(
                item.getProducto().getId(),
                item.getProducto().getNombre(),
                item.getProducto().getPrecio(),
                item.getCantidad(),
                item.getSubtotal()
        );
    }
}
