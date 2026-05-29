package com.pos.infraestructura;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos.aplicacion.puerto.CarritoServicioPuerto;
import com.pos.infraestructura.adaptador.entrada.CarritoControlador;
import com.pos.infraestructura.dto.AgregarItemRequest;
import com.pos.infraestructura.dto.CarritoDTO;
import com.pos.infraestructura.excepcion.CantidadInvalidaException;
import com.pos.infraestructura.excepcion.ProductoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración para {@link CarritoControlador} usando {@code @WebMvcTest} y {@code MockMvc}.
 *
 * <p>Requerimientos: 7.2, 12.2</p>
 */
@WebMvcTest(CarritoControlador.class)
class CarritoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarritoServicioPuerto carritoServicio;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // GET /api/carrito
    // -------------------------------------------------------------------------

    @Test
    void obtenerCarrito_retornaHttp200() throws Exception {
        CarritoDTO carritoVacio = new CarritoDTO(Collections.emptyList(), BigDecimal.ZERO);
        when(carritoServicio.obtenerCarrito()).thenReturn(carritoVacio);

        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // POST /api/carrito/items — body válido
    // -------------------------------------------------------------------------

    @Test
    void agregarItem_bodyValido_retornaHttp200() throws Exception {
        CarritoDTO carritoActualizado = new CarritoDTO(Collections.emptyList(), BigDecimal.ZERO);
        when(carritoServicio.agregarItem(any(UUID.class), anyInt())).thenReturn(carritoActualizado);

        AgregarItemRequest request = new AgregarItemRequest();
        request.setProductoId(UUID.randomUUID());
        request.setCantidad(2);

        mockMvc.perform(post("/api/carrito/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // POST /api/carrito/items — cantidad 0 → 400
    // -------------------------------------------------------------------------

    @Test
    void agregarItem_cantidadCero_retornaHttp400() throws Exception {
        when(carritoServicio.agregarItem(any(UUID.class), anyInt()))
                .thenThrow(new CantidadInvalidaException("La cantidad debe ser mayor que cero"));

        AgregarItemRequest request = new AgregarItemRequest();
        request.setProductoId(UUID.randomUUID());
        request.setCantidad(0);

        mockMvc.perform(post("/api/carrito/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /api/carrito/items — productoId inexistente → 404
    // -------------------------------------------------------------------------

    @Test
    void agregarItem_productoInexistente_retornaHttp404() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(carritoServicio.agregarItem(any(UUID.class), anyInt()))
                .thenThrow(new ProductoNoEncontradoException("Producto no encontrado: " + idInexistente));

        AgregarItemRequest request = new AgregarItemRequest();
        request.setProductoId(idInexistente);
        request.setCantidad(1);

        mockMvc.perform(post("/api/carrito/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/carrito
    // -------------------------------------------------------------------------

    @Test
    void vaciarCarrito_retornaHttp200() throws Exception {
        CarritoDTO carritoVacio = new CarritoDTO(Collections.emptyList(), BigDecimal.ZERO);
        when(carritoServicio.vaciarCarrito()).thenReturn(carritoVacio);

        mockMvc.perform(delete("/api/carrito"))
                .andExpect(status().isOk());
    }
}
