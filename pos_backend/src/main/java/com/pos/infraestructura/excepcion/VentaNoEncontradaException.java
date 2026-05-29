package com.pos.infraestructura.excepcion;

/**
 * Excepción lanzada cuando se solicita una venta que no existe en el historial.
 * Corresponde a una respuesta HTTP 404 Not Found.
 *
 * <p>Requerimientos: 5.3</p>
 */
public class VentaNoEncontradaException extends RuntimeException {

    /**
     * Crea una nueva excepción con el mensaje descriptivo indicado.
     *
     * @param message descripción del error.
     */
    public VentaNoEncontradaException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción con mensaje descriptivo y causa raíz.
     *
     * @param message descripción del error.
     * @param cause   excepción original que provocó este error.
     */
    public VentaNoEncontradaException(String message, Throwable cause) {
        super(message, cause);
    }
}
