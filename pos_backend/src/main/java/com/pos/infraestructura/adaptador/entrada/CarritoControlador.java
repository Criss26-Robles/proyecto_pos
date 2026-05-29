package com.pos.infraestructura.adaptador.entrada;

import com.pos.aplicacion.puerto.CarritoServicioPuerto;
import com.pos.infraestructura.dto.ActualizarItemRequest;
import com.pos.infraestructura.dto.AgregarItemRequest;
import com.pos.infraestructura.dto.CarritoDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST para la gestión del carrito de compras activo.
 *
 * <p>Expone los endpoints bajo {@code /api/carrito} y delega la lógica de negocio
 * al puerto de entrada {@link CarritoServicioPuerto}. Forma parte de la capa de
 * infraestructura (adaptador de entrada) en la arquitectura hexagonal.</p>
 *
 * <p>Requerimientos: 2.1–2.5, 4.4, 5.1, 5.4, 6.4</p>
 */
@RestController
@RequestMapping("/api/carrito")
public class CarritoControlador {

    private final CarritoServicioPuerto carritoServicio;

    /**
     * Constructor con inyección de dependencia del puerto de servicio del carrito.
     *
     * @param carritoServicio puerto de entrada que implementa los casos de uso del carrito.
     */
    public CarritoControlador(CarritoServicioPuerto carritoServicio) {
        this.carritoServicio = carritoServicio;
    }

    /**
     * Retorna el estado actual del carrito de compras.
     *
     * @return {@code 200 OK} con el {@link CarritoDTO} que contiene los ítems y el total actuales.
     */
    @GetMapping
    public ResponseEntity<CarritoDTO> obtenerCarrito() {
        CarritoDTO dto = carritoServicio.obtenerCarrito();
        return ResponseEntity.ok(dto);
    }

    /**
     * Agrega un producto al carrito con la cantidad indicada.
     *
     * <p>Si el producto ya existe en el carrito, incrementa su cantidad. El cuerpo de la
     * petición es validado con {@code @Valid}; si la validación falla se retorna HTTP 400.</p>
     *
     * @param request cuerpo de la petición con {@code productoId} y {@code cantidad}.
     * @return {@code 200 OK} con el {@link CarritoDTO} actualizado.
     * @throws com.pos.infraestructura.excepcion.ProductoNoEncontradoException
     *         si el producto no existe (manejado globalmente → 404).
     * @throws com.pos.infraestructura.excepcion.CantidadInvalidaException
     *         si la cantidad es inválida (manejado globalmente → 400).
     */
    @PostMapping("/items")
    public ResponseEntity<CarritoDTO> agregarItem(@Valid @RequestBody AgregarItemRequest request) {
        CarritoDTO dto = carritoServicio.agregarItem(request.getProductoId(), request.getCantidad());
        return ResponseEntity.ok(dto);
    }

    /**
     * Actualiza la cantidad de un producto ya presente en el carrito.
     *
     * <p>El cuerpo de la petición es validado con {@code @Valid}; si la validación falla
     * se retorna HTTP 400.</p>
     *
     * @param productoId cadena de texto que representa el UUID del producto a actualizar.
     * @param request    cuerpo de la petición con la nueva {@code cantidad}.
     * @return {@code 200 OK} con el {@link CarritoDTO} actualizado.
     * @throws com.pos.infraestructura.excepcion.ProductoNoEncontradoException
     *         si el producto no se encuentra en el carrito (manejado globalmente → 404).
     * @throws com.pos.infraestructura.excepcion.CantidadInvalidaException
     *         si la cantidad es inválida (manejado globalmente → 400).
     */
    @PutMapping("/items/{productoId}")
    public ResponseEntity<CarritoDTO> actualizarItem(
            @PathVariable String productoId,
            @Valid @RequestBody ActualizarItemRequest request) {

        CarritoDTO dto = carritoServicio.actualizarItem(
                UUID.fromString(productoId), request.getCantidad());
        return ResponseEntity.ok(dto);
    }

    /**
     * Elimina un producto del carrito.
     *
     * @param productoId cadena de texto que representa el UUID del producto a eliminar.
     * @return {@code 200 OK} con el {@link CarritoDTO} actualizado.
     * @throws com.pos.infraestructura.excepcion.ProductoNoEncontradoException
     *         si el producto no se encuentra en el carrito (manejado globalmente → 404).
     */
    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<CarritoDTO> eliminarItem(@PathVariable String productoId) {
        CarritoDTO dto = carritoServicio.eliminarItem(UUID.fromString(productoId));
        return ResponseEntity.ok(dto);
    }

    /**
     * Vacía completamente el carrito, eliminando todos los ítems y poniendo el total a cero.
     *
     * @return {@code 200 OK} con el {@link CarritoDTO} vacío (lista de ítems vacía y total 0.00).
     */
    @DeleteMapping
    public ResponseEntity<CarritoDTO> vaciarCarrito() {
        CarritoDTO dto = carritoServicio.vaciarCarrito();
        return ResponseEntity.ok(dto);
    }
}
