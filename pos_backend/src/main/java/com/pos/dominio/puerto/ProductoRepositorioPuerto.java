package com.pos.dominio.puerto;

import com.pos.dominio.modelo.Producto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (arquitectura hexagonal) para la persistencia de productos.
 *
 * <p>Define el contrato que cualquier adaptador de salida (repositorio) debe cumplir
 * para acceder al catálogo de productos. La capa de dominio depende únicamente de
 * esta interfaz, nunca de la implementación concreta.</p>
 *
 * <p>Valida: Requerimientos 1.6, 4.3</p>
 */
public interface ProductoRepositorioPuerto {

    /**
     * Retorna todos los productos disponibles en el catálogo.
     *
     * @return lista de productos; vacía si no hay ninguno.
     */
    List<Producto> findAll();

    /**
     * Busca un producto por su identificador único.
     *
     * @param id identificador UUID del producto.
     * @return {@link Optional} con el producto si existe, o vacío si no se encuentra.
     */
    Optional<Producto> findById(UUID id);

    /**
     * Busca productos cuyo nombre contenga el texto indicado (búsqueda parcial,
     * insensible a mayúsculas/minúsculas).
     *
     * @param nombre texto a buscar en el nombre del producto.
     * @return lista de productos coincidentes; vacía si no hay resultados.
     */
    List<Producto> findByNombre(String nombre);

    /**
     * Persiste un producto en el repositorio.
     * Usado principalmente para cargar datos iniciales del catálogo.
     *
     * @param producto producto a guardar; no debe ser {@code null}.
     */
    void guardar(Producto producto);
}
