package com.pos.infraestructura;

import com.pos.aplicacion.puerto.ProductoServicioPuerto;
import com.pos.infraestructura.adaptador.entrada.ProductoControlador;
import com.pos.infraestructura.dto.ProductoDTO;
import com.pos.infraestructura.excepcion.ProductoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración para {@link ProductoControlador} usando {@code @WebMvcTest} y {@code MockMvc}.
 *
 * <p>Requerimientos: 7.2, 12.2</p>
 */
@WebMvcTest(ProductoControlador.class)
class ProductoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoServicioPuerto productoServicio;

    // -------------------------------------------------------------------------
    // GET /api/productos
    // -------------------------------------------------------------------------

    @Test
    void listarTodos_retornaHttp200YListaJson() throws Exception {
        ProductoDTO dto = new ProductoDTO(
                UUID.randomUUID(), "COD-001", "Coca Cola", new BigDecimal("1.50"), 100);
        when(productoServicio.listarTodos()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // -------------------------------------------------------------------------
    // GET /api/productos?nombre=X
    // -------------------------------------------------------------------------

    @Test
    void buscarPorNombre_retornaHttp200() throws Exception {
        ProductoDTO dto = new ProductoDTO(
                UUID.randomUUID(), "COD-001", "Coca Cola", new BigDecimal("1.50"), 100);
        when(productoServicio.buscarPorNombre("Coca")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/productos").param("nombre", "Coca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // -------------------------------------------------------------------------
    // GET /api/productos/{id} — existente
    // -------------------------------------------------------------------------

    @Test
    void buscarPorId_existente_retornaHttp200YNombre() throws Exception {
        UUID id = UUID.randomUUID();
        ProductoDTO dto = new ProductoDTO(
                id, "COD-002", "Pepsi", new BigDecimal("1.40"), 50);
        when(productoServicio.buscarPorId(id)).thenReturn(dto);

        mockMvc.perform(get("/api/productos/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Pepsi"));
    }

    // -------------------------------------------------------------------------
    // GET /api/productos/{id} — inexistente
    // -------------------------------------------------------------------------

    @Test
    void buscarPorId_inexistente_retornaHttp404() throws Exception {
        UUID id = UUID.randomUUID();
        when(productoServicio.buscarPorId(id))
                .thenThrow(new ProductoNoEncontradoException("Producto no encontrado: " + id));

        mockMvc.perform(get("/api/productos/{id}", id.toString()))
                .andExpect(status().isNotFound());
    }
}
