package com.pos.aplicacion.puerto;

import com.pos.infraestructura.dto.VentaDTO;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (driving port) para las operaciones sobre ventas.
 * Define el contrato que los adaptadores de entrada (controladores REST) deben
 * invocar para confirmar ventas y consultar el historial de transacciones.
 *
 * <p>Pertenece a la capa de aplicación en la arquitectura hexagonal (Ports &amp; Adapters).
 * Las implementaciones concretas residen en {@code com.pos.aplicacion.servicio}.</p>
 *
 * <p>Requerimientos: 4.2</p>
 */
public interface VentaServicioPuerto {

    /**
     * Confirma la venta con los ítems actuales del carrito, genera un registro de venta
     * con UUID único y marca de tiempo, y vacía el carrito tras la confirmación.
     *
     * @return {@link VentaDTO} con los datos de la venta recién creada.
     * @throws com.pos.infraestructura.excepcion.CarritoVacioException
     *         si el carrito no contiene ningún ítem al momento de confirmar.
     */
    VentaDTO confirmarVenta();

    /**
     * Retorna el historial completo de ventas confirmadas.
     *
     * @return lista de {@link VentaDTO} con todas las ventas registradas; nunca {@code null}.
     */
    List<VentaDTO> listarVentas();

    /**
     * Busca una venta por su identificador único.
     *
     * @param id identificador UUID de la venta.
     * @return el {@link VentaDTO} correspondiente al ID indicado.
     * @throws com.pos.infraestructura.excepcion.VentaNoEncontradaException
     *         si no existe ninguna venta con ese ID.
     */
    VentaDTO buscarVentaPorId(UUID id);
}
