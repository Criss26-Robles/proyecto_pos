package com.pos.aplicacion.servicio;

import com.pos.aplicacion.puerto.ProductoServicioPuerto;
import com.pos.dominio.modelo.Producto;
import com.pos.dominio.puerto.ProductoRepositorioPuerto;
import com.pos.infraestructura.dto.ProductoDTO;
import com.pos.infraestructura.excepcion.ProductoNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para la gestión del catálogo de productos.
 * Implementa el puerto de entrada {@link ProductoServicioPuerto} y delega
 * la persistencia al puerto de salida {@link ProductoRepositorioPuerto}.
 *
 * <p>Requerimientos: 1.1, 1.2, 1.5, 4.4, 4.5</p>
 */
@Service
public class ProductoServicio implements ProductoServicioPuerto {

    private final ProductoRepositorioPuerto productoRepositorio;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param productoRepositorio puerto de salida para acceder al catálogo de productos.
     */
    public ProductoServicio(ProductoRepositorioPuerto productoRepositorio) {
        this.productoRepositorio = productoRepositorio;
    }

    /**
     * Retorna la lista completa de productos disponibles en el catálogo.
     *
     * @return lista de {@link ProductoDTO}; nunca {@code null}.
     */
    @Override
    public List<ProductoDTO> listarTodos() {
        return productoRepositorio.findAll()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un producto por su identificador único.
     *
     * @param id identificador UUID del producto.
     * @return el {@link ProductoDTO} correspondiente.
     * @throws ProductoNoEncontradoException si no existe ningún producto con ese ID.
     */
    @Override
    public ProductoDTO buscarPorId(UUID id) {
        return productoRepositorio.findById(id)
                .map(this::mapearADTO)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "Producto no encontrado con id: " + id));
    }

    /**
     * Busca productos cuyo nombre contenga el texto indicado (insensible a mayúsculas).
     *
     * @param nombre texto parcial o completo a buscar.
     * @return lista de {@link ProductoDTO} coincidentes; puede estar vacía.
     */
    @Override
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepositorio.findByNombre(nombre)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Métodos privados de mapeo
    // -------------------------------------------------------------------------

    /**
     * Convierte una entidad {@link Producto} en su representación DTO.
     *
     * @param p entidad de dominio a convertir.
     * @return {@link ProductoDTO} con los datos del producto.
     */
    private ProductoDTO mapearADTO(Producto p) {
        return new ProductoDTO(p.getId(), p.getCodigo(), p.getNombre(), p.getPrecio(), p.getStock());
    }
}
