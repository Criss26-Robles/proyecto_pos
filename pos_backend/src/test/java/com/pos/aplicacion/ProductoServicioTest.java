package com.pos.aplicacion;

import com.pos.aplicacion.servicio.ProductoServicio;
import com.pos.dominio.modelo.Producto;
import com.pos.dominio.puerto.ProductoRepositorioPuerto;
import com.pos.infraestructura.dto.ProductoDTO;
import com.pos.infraestructura.excepcion.ProductoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link ProductoServicio}.
 * Valida los casos de uso del catálogo de productos usando Mockito para simular
 * el puerto de salida {@link ProductoRepositorioPuerto}.
 *
 * <p>Requerimientos: 7.1, 12.1</p>
 */
@ExtendWith(MockitoExtension.class)
class ProductoServicioTest {

    @Mock
    private ProductoRepositorioPuerto productoRepositorio;

    @InjectMocks
    private ProductoServicio productoServicio;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Producto crearProducto(UUID id, String codigo, String nombre, BigDecimal precio, int stock) {
        return new Producto(id, codigo, nombre, precio, stock);
    }

    // -------------------------------------------------------------------------
    // listarTodos
    // -------------------------------------------------------------------------

    @Test
    void listarTodos_retornaListaDeProductos() {
        // Arrange
        Producto p1 = crearProducto(UUID.randomUUID(), "P001", "Manzana", new BigDecimal("1.50"), 100);
        Producto p2 = crearProducto(UUID.randomUUID(), "P002", "Naranja", new BigDecimal("2.00"), 80);
        when(productoRepositorio.findAll()).thenReturn(Arrays.asList(p1, p2));

        // Act
        List<ProductoDTO> resultado = productoServicio.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Manzana", resultado.get(0).getNombre());
        assertEquals("Naranja", resultado.get(1).getNombre());
    }

    // -------------------------------------------------------------------------
    // buscarPorId
    // -------------------------------------------------------------------------

    @Test
    void buscarPorId_existente_retornaProductoDTO() {
        // Arrange
        UUID id = UUID.randomUUID();
        Producto producto = crearProducto(id, "P003", "Pera", new BigDecimal("3.00"), 50);
        when(productoRepositorio.findById(id)).thenReturn(Optional.of(producto));

        // Act
        ProductoDTO resultado = productoServicio.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Pera", resultado.getNombre());
        assertEquals(new BigDecimal("3.00"), resultado.getPrecio());
    }

    @Test
    void buscarPorId_inexistente_lanzaExcepcion() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(productoRepositorio.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductoNoEncontradoException.class,
                () -> productoServicio.buscarPorId(id));
    }

    // -------------------------------------------------------------------------
    // buscarPorNombre
    // -------------------------------------------------------------------------

    @Test
    void buscarPorNombre_conCoincidencias_retornaLista() {
        // Arrange
        Producto p = crearProducto(UUID.randomUUID(), "P004", "Mango", new BigDecimal("4.50"), 30);
        when(productoRepositorio.findByNombre("mango")).thenReturn(Collections.singletonList(p));

        // Act
        List<ProductoDTO> resultado = productoServicio.buscarPorNombre("mango");

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Mango", resultado.get(0).getNombre());
    }

    @Test
    void buscarPorNombre_sinCoincidencias_retornaListaVacia() {
        // Arrange
        when(productoRepositorio.findByNombre("xyz")).thenReturn(Collections.emptyList());

        // Act
        List<ProductoDTO> resultado = productoServicio.buscarPorNombre("xyz");

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
