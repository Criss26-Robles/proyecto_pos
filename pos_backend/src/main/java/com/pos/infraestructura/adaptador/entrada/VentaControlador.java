package com.pos.infraestructura.adaptador.entrada;

import com.pos.aplicacion.puerto.VentaServicioPuerto;
import com.pos.infraestructura.dto.VentaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de ventas confirmadas.
 *
 * <p>Expone los endpoints bajo {@code /api/ventas} y delega la lógica de negocio
 * al puerto de entrada {@link VentaServicioPuerto}. Forma parte de la capa de
 * infraestructura (adaptador de entrada) en la arquitectura hexagonal.</p>
 *
 * <p>Requerimientos: 3.1, 3.2, 3.4, 3.5, 3.6, 4.4, 5.1, 5.4</p>
 */
@RestController
@RequestMapping("/api/ventas")
public class VentaControlador {

    private final VentaServicioPuerto ventaServicio;

    /**
     * Constructor con inyección de dependencia del puerto de servicio de ventas.
     *
     * @param ventaServicio puerto de entrada que implementa los casos de uso de ventas.
     */
    public VentaControlador(VentaServicioPuerto ventaServicio) {
        this.ventaServicio = ventaServicio;
    }

    /**
     * Confirma la venta con los ítems actuales del carrito.
     *
     * <p>Genera un registro de venta con UUID único y marca de tiempo, y vacía el carrito
     * tras la confirmación exitosa. Retorna HTTP 201 (Created) con los datos de la venta.</p>
     *
     * @return {@code 201 Created} con el {@link VentaDTO} de la venta recién confirmada.
     * @throws com.pos.infraestructura.excepcion.CarritoVacioException
     *         si el carrito no contiene ningún ítem (manejado globalmente → 400).
     */
    @PostMapping
    public ResponseEntity<VentaDTO> confirmarVenta() {
        VentaDTO dto = ventaServicio.confirmarVenta();
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Retorna el historial completo de ventas confirmadas.
     *
     * @return {@code 200 OK} con la lista de {@link VentaDTO}; la lista puede estar vacía.
     */
    @GetMapping
    public ResponseEntity<List<VentaDTO>> listarVentas() {
        List<VentaDTO> lista = ventaServicio.listarVentas();
        return ResponseEntity.ok(lista);
    }

    /**
     * Obtiene una venta específica por su identificador UUID.
     *
     * @param id cadena de texto que representa el UUID de la venta.
     * @return {@code 200 OK} con el {@link VentaDTO} encontrado.
     * @throws com.pos.infraestructura.excepcion.VentaNoEncontradaException
     *         si no existe ninguna venta con el ID indicado (manejado globalmente → 404).
     * @throws IllegalArgumentException si {@code id} no tiene formato UUID válido.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> buscarPorId(@PathVariable String id) {
        VentaDTO dto = ventaServicio.buscarVentaPorId(UUID.fromString(id));
        return ResponseEntity.ok(dto);
    }
}
