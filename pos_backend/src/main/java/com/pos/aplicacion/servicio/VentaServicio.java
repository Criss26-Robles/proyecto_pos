package com.pos.aplicacion.servicio;

import com.pos.aplicacion.puerto.VentaServicioPuerto;
import com.pos.dominio.modelo.Carrito;
import com.pos.dominio.modelo.ItemCarrito;
import com.pos.dominio.modelo.Venta;
import com.pos.dominio.puerto.CarritoRepositorioPuerto;
import com.pos.dominio.puerto.VentaRepositorioPuerto;
import com.pos.infraestructura.dto.ItemCarritoDTO;
import com.pos.infraestructura.dto.VentaDTO;
import com.pos.infraestructura.excepcion.CarritoVacioException;
import com.pos.infraestructura.excepcion.VentaNoEncontradaException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para la gestión de ventas.
 * Implementa el puerto de entrada {@link VentaServicioPuerto} y delega
 * la persistencia a los puertos de salida {@link CarritoRepositorioPuerto}
 * y {@link VentaRepositorioPuerto}.
 *
 * <p>Requerimientos: 3.1–3.9, 4.4, 4.5</p>
 */
@Service
public class VentaServicio implements VentaServicioPuerto {

    private final CarritoRepositorioPuerto carritoRepositorio;
    private final VentaRepositorioPuerto ventaRepositorio;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param carritoRepositorio puerto de salida para acceder y vaciar el carrito.
     * @param ventaRepositorio   puerto de salida para persistir y consultar ventas.
     */
    public VentaServicio(CarritoRepositorioPuerto carritoRepositorio,
                         VentaRepositorioPuerto ventaRepositorio) {
        this.carritoRepositorio = carritoRepositorio;
        this.ventaRepositorio = ventaRepositorio;
    }

    /**
     * Confirma la venta con los ítems actuales del carrito, genera un registro de venta
     * con UUID único y marca de tiempo, y vacía el carrito tras la confirmación.
     *
     * @return {@link VentaDTO} con los datos de la venta recién creada.
     * @throws CarritoVacioException si el carrito no contiene ningún ítem.
     */
    @Override
    public VentaDTO confirmarVenta() {
        Carrito carrito = carritoRepositorio.obtener();
        if (carrito.estaVacio()) {
            throw new CarritoVacioException(
                    "El carrito está vacío. Agregue productos antes de confirmar la venta.");
        }
        Venta venta = new Venta(
                UUID.randomUUID(),
                new ArrayList<>(carrito.getItems()),
                carrito.getTotal(),
                LocalDateTime.now()
        );
        ventaRepositorio.guardar(venta);
        carritoRepositorio.vaciar();
        return mapearADTO(venta);
    }

    /**
     * Retorna el historial completo de ventas confirmadas.
     *
     * @return lista de {@link VentaDTO} con todas las ventas registradas; nunca {@code null}.
     */
    @Override
    public List<VentaDTO> listarVentas() {
        return ventaRepositorio.findAll()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca una venta por su identificador único.
     *
     * @param id identificador UUID de la venta.
     * @return el {@link VentaDTO} correspondiente.
     * @throws VentaNoEncontradaException si no existe ninguna venta con ese ID.
     */
    @Override
    public VentaDTO buscarVentaPorId(UUID id) {
        return ventaRepositorio.findById(id)
                .map(this::mapearADTO)
                .orElseThrow(() -> new VentaNoEncontradaException(
                        "Venta no encontrada con id: " + id));
    }

    // -------------------------------------------------------------------------
    // Métodos privados de mapeo
    // -------------------------------------------------------------------------

    /**
     * Convierte una entidad {@link Venta} en su representación DTO.
     *
     * @param venta entidad de dominio a convertir.
     * @return {@link VentaDTO} con id, ítems mapeados, total y fecha/hora.
     */
    private VentaDTO mapearADTO(Venta venta) {
        List<ItemCarritoDTO> itemsDTO = venta.getItems()
                .stream()
                .map(this::mapearItemADTO)
                .collect(Collectors.toList());
        return new VentaDTO(
                venta.getId(),
                itemsDTO,
                venta.getTotal(),
                venta.getFechaHora().toString()
        );
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
