package com.pos.infraestructura.adaptador.salida;

import com.pos.dominio.modelo.Producto;
import com.pos.dominio.puerto.ProductoRepositorioPuerto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de salida (arquitectura hexagonal) que implementa {@link ProductoRepositorioPuerto}
 * usando un mapa en memoria como almacenamiento.
 *
 * <p>Requerimientos: 1.7, 4.5</p>
 */
@Repository
public class ProductoRepositorioMemoria implements ProductoRepositorioPuerto {

    private final Map<UUID, Producto> productos = new LinkedHashMap<>();

    /**
     * Retorna todos los productos del catálogo.
     *
     * @return lista de productos; vacía si no hay ninguno.
     */
    @Override
    public List<Producto> findAll() {
        return new ArrayList<>(productos.values());
    }

    /**
     * Busca un producto por su identificador único.
     *
     * @param id identificador UUID del producto.
     * @return {@link Optional} con el producto si existe, o vacío si no se encuentra.
     */
    @Override
    public Optional<Producto> findById(UUID id) {
        return Optional.ofNullable(productos.get(id));
    }

    /**
     * Busca productos cuyo nombre contenga el texto indicado (búsqueda parcial,
     * insensible a mayúsculas/minúsculas).
     *
     * @param nombre texto a buscar en el nombre del producto.
     * @return lista de productos coincidentes; vacía si no hay resultados.
     */
    @Override
    public List<Producto> findByNombre(String nombre) {
        return productos.values().stream()
                .filter(producto -> producto.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Persiste un producto en el almacenamiento en memoria.
     * Usado principalmente por {@code DatosIniciales} para cargar el catálogo inicial.
     *
     * @param producto producto a guardar; no debe ser {@code null}.
     */
    @Override
    public void guardar(Producto producto) {
        productos.put(producto.getId(), producto);
    }
}
