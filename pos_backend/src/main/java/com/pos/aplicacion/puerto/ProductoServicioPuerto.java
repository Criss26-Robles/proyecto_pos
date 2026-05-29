package com.pos.aplicacion.puerto;

import com.pos.infraestructura.dto.ProductoDTO;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (driving port) para las operaciones sobre productos.
 * Define el contrato que los adaptadores de entrada (controladores REST) deben
 * invocar para acceder a los casos de uso relacionados con el catálogo de productos.
 *
 * <p>Pertenece a la capa de aplicación en la arquitectura hexagonal (Ports &amp; Adapters).
 * Las implementaciones concretas residen en {@code com.pos.aplicacion.servicio}.</p>
 *
 * <p>Requerimientos: 4.2</p>
 */
public interface ProductoServicioPuerto {

    /**
     * Retorna la lista completa de productos disponibles en el catálogo.
     *
     * @return lista de {@link ProductoDTO} con todos los productos; nunca {@code null}.
     */
    List<ProductoDTO> listarTodos();

    /**
     * Busca un producto por su identificador único.
     *
     * @param id identificador UUID del producto.
     * @return el {@link ProductoDTO} correspondiente al ID indicado.
     * @throws com.pos.infraestructura.excepcion.ProductoNoEncontradoException
     *         si no existe ningún producto con ese ID.
     */
    ProductoDTO buscarPorId(UUID id);

    /**
     * Busca productos cuyo nombre contenga el texto indicado (insensible a mayúsculas).
     *
     * @param nombre texto parcial o completo a buscar en el nombre del producto.
     * @return lista de {@link ProductoDTO} que coinciden con el criterio; puede estar vacía.
     */
    List<ProductoDTO> buscarPorNombre(String nombre);
}
