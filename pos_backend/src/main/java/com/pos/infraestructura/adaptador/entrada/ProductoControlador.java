package com.pos.infraestructura.adaptador.entrada;

import com.pos.aplicacion.puerto.ProductoServicioPuerto;
import com.pos.infraestructura.dto.ProductoDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión del catálogo de productos.
 *
 * <p>Expone los endpoints bajo {@code /api/productos} y delega la lógica de negocio
 * al puerto de entrada {@link ProductoServicioPuerto}. Forma parte de la capa de
 * infraestructura (adaptador de entrada) en la arquitectura hexagonal.</p>
 *
 * <p>Requerimientos: 1.1, 1.2, 1.5, 4.4, 5.1, 5.4</p>
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {

    private final ProductoServicioPuerto productoServicio;

    /**
     * Constructor con inyección de dependencia del puerto de servicio de productos.
     *
     * @param productoServicio puerto de entrada que implementa los casos de uso de productos.
     */
    public ProductoControlador(ProductoServicioPuerto productoServicio) {
        this.productoServicio = productoServicio;
    }

    /**
     * Lista todos los productos del catálogo o filtra por nombre si se proporciona el parámetro.
     *
     * <p>Si el parámetro {@code nombre} está presente, realiza una búsqueda insensible a
     * mayúsculas/minúsculas por nombre parcial. Si no se proporciona, retorna todos los
     * productos disponibles.</p>
     *
     * @param nombre texto opcional para filtrar productos por nombre (puede ser {@code null}).
     * @return {@code 200 OK} con la lista de {@link ProductoDTO}; la lista puede estar vacía.
     */
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarOBuscar(
            @RequestParam(required = false) String nombre) {

        List<ProductoDTO> resultado;
        if (nombre != null) {
            resultado = productoServicio.buscarPorNombre(nombre);
        } else {
            resultado = productoServicio.listarTodos();
        }
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene un producto específico por su identificador UUID.
     *
     * @param id cadena de texto que representa el UUID del producto.
     * @return {@code 200 OK} con el {@link ProductoDTO} encontrado.
     * @throws com.pos.infraestructura.excepcion.ProductoNoEncontradoException
     *         si no existe ningún producto con el ID indicado (manejado globalmente → 404).
     * @throws IllegalArgumentException si {@code id} no tiene formato UUID válido.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        ProductoDTO dto = productoServicio.buscarPorId(uuid);
        return ResponseEntity.ok(dto);
    }
}
