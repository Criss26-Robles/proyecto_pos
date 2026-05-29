package com.pos.infraestructura.adaptador.salida;

import com.pos.dominio.modelo.Venta;
import com.pos.dominio.puerto.VentaRepositorioPuerto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de salida (arquitectura hexagonal) que implementa {@link VentaRepositorioPuerto}
 * usando un mapa en memoria como almacenamiento del historial de ventas.
 *
 * <p>Requerimientos: 4.5</p>
 */
@Repository
public class VentaRepositorioMemoria implements VentaRepositorioPuerto {

    private final Map<UUID, Venta> ventas = new LinkedHashMap<>();

    /**
     * Persiste una nueva venta en el historial en memoria.
     *
     * @param venta instancia de la venta a guardar; no debe ser {@code null}.
     * @return la {@link Venta} guardada.
     */
    @Override
    public Venta guardar(Venta venta) {
        ventas.put(venta.getId(), venta);
        return venta;
    }

    /**
     * Retorna el historial completo de ventas registradas.
     *
     * @return lista de ventas en orden de inserción; vacía si no se ha realizado ninguna.
     */
    @Override
    public List<Venta> findAll() {
        return new ArrayList<>(ventas.values());
    }

    /**
     * Busca una venta por su identificador único.
     *
     * @param id identificador UUID de la venta.
     * @return {@link Optional} con la venta si existe, o vacío si no se encuentra.
     */
    @Override
    public Optional<Venta> findById(UUID id) {
        return Optional.ofNullable(ventas.get(id));
    }
}
