package com.pos.infraestructura;

import com.pos.aplicacion.puerto.VentaServicioPuerto;
import com.pos.infraestructura.adaptador.entrada.VentaControlador;
import com.pos.infraestructura.dto.VentaDTO;
import com.pos.infraestructura.excepcion.CarritoVacioException;
import com.pos.infraestructura.excepcion.VentaNoEncontradaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración para {@link VentaControlador} usando {@code @WebMvcTest} y {@code MockMvc}.
 *
 * <p>Requerimientos: 7.2, 12.2</p>
 */
@WebMvcTest(VentaControlador.class)
class VentaControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VentaServicioPuerto ventaServicio;

    // -------------------------------------------------------------------------
    // POST /api/ventas — exitoso → 201
    // -------------------------------------------------------------------------

    @Test
    void confirmarVenta_exitoso_retornaHttp201() throws Exception {
        VentaDTO ventaDto = new VentaDTO(
                UUID.randomUUID(),
                Collections.emptyList(),
                BigDecimal.TEN,
                LocalDateTime.now().toString());
        when(ventaServicio.confirmarVenta()).thenReturn(ventaDto);

        mockMvc.perform(post("/api/ventas"))
                .andExpect(status().isCreated());
    }

    // -------------------------------------------------------------------------
    // POST /api/ventas — carrito vacío → 400
    // -------------------------------------------------------------------------

    @Test
    void confirmarVenta_carritoVacio_retornaHttp400() throws Exception {
        when(ventaServicio.confirmarVenta())
                .thenThrow(new CarritoVacioException("El carrito está vacío"));

        mockMvc.perform(post("/api/ventas"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/ventas → 200
    // -------------------------------------------------------------------------

    @Test
    void listarVentas_retornaHttp200() throws Exception {
        VentaDTO ventaDto = new VentaDTO(
                UUID.randomUUID(),
                Collections.emptyList(),
                BigDecimal.TEN,
                LocalDateTime.now().toString());
        when(ventaServicio.listarVentas()).thenReturn(List.of(ventaDto));

        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // GET /api/ventas/{id} — existente → 200
    // -------------------------------------------------------------------------

    @Test
    void buscarVentaPorId_existente_retornaHttp200() throws Exception {
        UUID id = UUID.randomUUID();
        VentaDTO ventaDto = new VentaDTO(
                id,
                Collections.emptyList(),
                BigDecimal.TEN,
                LocalDateTime.now().toString());
        when(ventaServicio.buscarVentaPorId(id)).thenReturn(ventaDto);

        mockMvc.perform(get("/api/ventas/{id}", id.toString()))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // GET /api/ventas/{id} — inexistente → 404
    // -------------------------------------------------------------------------

    @Test
    void buscarVentaPorId_inexistente_retornaHttp404() throws Exception {
        UUID id = UUID.randomUUID();
        when(ventaServicio.buscarVentaPorId(id))
                .thenThrow(new VentaNoEncontradaException("Venta no encontrada: " + id));

        mockMvc.perform(get("/api/ventas/{id}", id.toString()))
                .andExpect(status().isNotFound());
    }
}
