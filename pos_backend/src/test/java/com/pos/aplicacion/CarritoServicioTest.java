package com.pos.aplicacion;

import com.pos.aplicacion.servicio.CarritoServicio;
import com.pos.dominio.modelo.Carrito;
import com.pos.dominio.modelo.ItemCarrito;
import com.pos.dominio.modelo.Producto;
import com.pos.dominio.puerto.CarritoRepositorioPuerto;
import com.pos.dominio.puerto.ProductoRepositorioPuerto;
import com.pos.infraestructura.dto.CarritoDTO;
import com.pos.infraestructura.excepcion.CantidadInvalidaException;
import com.pos.infraestructura.excepcion.ProductoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link CarritoServicio}.
 * Valida los casos de uso del carrito de compras usando Mockito para simular
 * los puertos de salida {@link ProductoRepositorioPuerto} y {@link CarritoRepositorioPuerto}.
 *
 * <p>Requerimientos: 7.1, 12.1</p>
 */
@ExtendWith(MockitoExtension.class)
class CarritoServicioTest {

    @Mock
    private ProductoRepositorioPuerto productoRepositorio;

    @Mock
    private CarritoRepositorioPuerto carritoRepositorio;

    @InjectMocks
    private CarritoServicio carritoServicio;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Producto crearProducto() {
        return new Producto(UUID.randomUUID(), "P001", "Manzana", new BigDecimal("1.50"), 100);
    }

    private Carrito carritoVacio() {
        return new Carrito();
    }

    private Carrito carritoConUnItem(Producto producto) {
        Carrito carrito = new Carrito();
        carrito.agregarItem(producto, 2);
        return carrito;
    }

    // -------------------------------------------------------------------------
    // agregarItem
    // -------------------------------------------------------------------------

    @Test
    void agregarItem_productoExistente_retornaCarritoActualizado() {
        // Arrange
        Producto producto = crearProducto();
        when(productoRepositorio.findById(producto.getId())).thenReturn(Optional.of(producto));
        when(carritoRepositorio.obtener()).thenReturn(carritoVacio());

        // Act
        CarritoDTO resultado = carritoServicio.agregarItem(producto.getId(), 3);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getItems().size());
        assertEquals(3, resultado.getItems().get(0).getCantidad());
        verify(carritoRepositorio).guardar(any(Carrito.class));
    }

    @Test
    void agregarItem_productoInexistente_lanzaExcepcion() {
        // Arrange
        UUID idInexistente = UUID.randomUUID();
        when(productoRepositorio.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductoNoEncontradoException.class,
                () -> carritoServicio.agregarItem(idInexistente, 1));
    }

    @Test
    void agregarItem_cantidadInvalida_lanzaExcepcion() {
        // Arrange
        UUID productoId = UUID.randomUUID();

        // Act & Assert
        assertThrows(CantidadInvalidaException.class,
                () -> carritoServicio.agregarItem(productoId, 0));
    }

    // -------------------------------------------------------------------------
    // actualizarItem
    // -------------------------------------------------------------------------

    @Test
    void actualizarItem_cantidadValida_retornaCarritoActualizado() {
        // Arrange
        Producto producto = crearProducto();
        Carrito carrito = carritoConUnItem(producto);
        when(carritoRepositorio.obtener()).thenReturn(carrito);

        // Act
        CarritoDTO resultado = carritoServicio.actualizarItem(producto.getId(), 5);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getItems().size());
        assertEquals(5, resultado.getItems().get(0).getCantidad());
        verify(carritoRepositorio).guardar(any(Carrito.class));
    }

    @Test
    void actualizarItem_cantidadInvalida_lanzaExcepcion() {
        // Arrange
        UUID productoId = UUID.randomUUID();

        // Act & Assert
        assertThrows(CantidadInvalidaException.class,
                () -> carritoServicio.actualizarItem(productoId, -1));
    }

    // -------------------------------------------------------------------------
    // eliminarItem
    // -------------------------------------------------------------------------

    @Test
    void eliminarItem_retornaCarritoActualizado() {
        // Arrange
        Producto producto = crearProducto();
        Carrito carrito = carritoConUnItem(producto);
        when(carritoRepositorio.obtener()).thenReturn(carrito);

        // Act
        CarritoDTO resultado = carritoServicio.eliminarItem(producto.getId());

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getItems().isEmpty());
        verify(carritoRepositorio).guardar(any(Carrito.class));
    }

    // -------------------------------------------------------------------------
    // vaciarCarrito
    // -------------------------------------------------------------------------

    @Test
    void vaciarCarrito_retornaCarritoVacio() {
        // Arrange
        when(carritoRepositorio.obtener()).thenReturn(carritoVacio());

        // Act
        carritoServicio.vaciarCarrito();

        // Assert
        verify(carritoRepositorio).vaciar();
    }

    // -------------------------------------------------------------------------
    // obtenerCarrito
    // -------------------------------------------------------------------------

    @Test
    void obtenerCarrito_retornaCarritoDTO() {
        // Arrange
        when(carritoRepositorio.obtener()).thenReturn(carritoVacio());

        // Act
        CarritoDTO resultado = carritoServicio.obtenerCarrito();

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getItems());
        assertTrue(resultado.getItems().isEmpty());
    }
}
