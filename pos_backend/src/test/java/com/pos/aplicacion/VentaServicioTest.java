package com.pos.aplicacion;

import com.pos.aplicacion.servicio.VentaServicio;
import com.pos.dominio.modelo.Carrito;
import com.pos.dominio.modelo.ItemCarrito;
import com.pos.dominio.modelo.Producto;
import com.pos.dominio.modelo.Venta;
import com.pos.dominio.puerto.CarritoRepositorioPuerto;
import com.pos.dominio.puerto.VentaRepositorioPuerto;
import com.pos.infraestructura.dto.VentaDTO;
import com.pos.infraestructura.excepcion.CarritoVacioException;
import com.pos.infraestructura.excepcion.VentaNoEncontradaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link VentaServicio}.
 * Valida los casos de uso de gestión de ventas usando Mockito para simular
 * los puertos de salida {@link CarritoRepositorioPuerto} y {@link VentaRepositorioPuerto}.
 *
 * <p>Requerimientos: 7.1, 12.1</p>
 */
@ExtendWith(MockitoExtension.class)
class VentaServicioTest {

    @Mock
    private CarritoRepositorioPuerto carritoRepositorio;

    @Mock
    private VentaRepositorioPuerto ventaRepositorio;

    @InjectMocks
    private VentaServicio ventaServicio;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Producto crearProducto() {
        return new Producto(UUID.randomUUID(), "P001", "Manzana", new BigDecimal("2.50"), 100);
    }

    private Carrito carritoConUnItem() {
        Producto producto = crearProducto();
        Carrito carrito = new Carrito();
        carrito.agregarItem(producto, 2);
        return carrito;
    }

    private Venta crearVenta(Carrito carrito) {
        return new Venta(
                UUID.randomUUID(),
                carrito.getItems(),
                carrito.getTotal(),
                LocalDateTime.now()
        );
    }

    // -------------------------------------------------------------------------
    // confirmarVenta
    // -------------------------------------------------------------------------

    @Test
    void confirmarVenta_carritoNoVacio_retornaVentaDTO() {
        // Arrange
        Carrito carrito = carritoConUnItem();
        Venta ventaGuardada = crearVenta(carrito);
        when(carritoRepositorio.obtener()).thenReturn(carrito);
        when(ventaRepositorio.guardar(any(Venta.class))).thenReturn(ventaGuardada);

        // Act
        VentaDTO resultado = ventaServicio.confirmarVenta();

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(1, resultado.getItems().size());
        verify(carritoRepositorio).vaciar();
    }

    @Test
    void confirmarVenta_carritoVacio_lanzaExcepcion() {
        // Arrange
        Carrito carritoVacio = new Carrito();
        when(carritoRepositorio.obtener()).thenReturn(carritoVacio);

        // Act & Assert
        assertThrows(CarritoVacioException.class,
                () -> ventaServicio.confirmarVenta());
    }

    // -------------------------------------------------------------------------
    // listarVentas
    // -------------------------------------------------------------------------

    @Test
    void listarVentas_retornaListaDeVentas() {
        // Arrange
        Carrito carrito = carritoConUnItem();
        Venta venta = crearVenta(carrito);
        when(ventaRepositorio.findAll()).thenReturn(Collections.singletonList(venta));

        // Act
        List<VentaDTO> resultado = ventaServicio.listarVentas();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertNotNull(resultado.get(0).getId());
    }

    // -------------------------------------------------------------------------
    // buscarVentaPorId
    // -------------------------------------------------------------------------

    @Test
    void buscarVentaPorId_existente_retornaVentaDTO() {
        // Arrange
        Carrito carrito = carritoConUnItem();
        Venta venta = crearVenta(carrito);
        when(ventaRepositorio.findById(venta.getId())).thenReturn(Optional.of(venta));

        // Act
        VentaDTO resultado = ventaServicio.buscarVentaPorId(venta.getId());

        // Assert
        assertNotNull(resultado);
        assertEquals(venta.getId(), resultado.getId());
        assertEquals(venta.getTotal(), resultado.getTotal());
    }

    @Test
    void buscarVentaPorId_inexistente_lanzaExcepcion() {
        // Arrange
        UUID idInexistente = UUID.randomUUID();
        when(ventaRepositorio.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(VentaNoEncontradaException.class,
                () -> ventaServicio.buscarVentaPorId(idInexistente));
    }
}
