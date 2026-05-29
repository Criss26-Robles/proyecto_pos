package com.pos.infraestructura.excepcion;

/**
 * Excepción lanzada cuando se intenta confirmar una venta con el carrito vacío.
 * Corresponde a una respuesta HTTP 400 Bad Request.
 *
 * <p>Requerimientos: 5.3</p>
 */
public class CarritoVacioException extends RuntimeException {

    /**
     * Crea una nueva excepción con el mensaje descriptivo indicado.
     *
     * @param message descripción del error.
     */
    public CarritoVacioException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción con mensaje descriptivo y causa raíz.
     *
     * @param message descripción del error.
     * @param cause   excepción original que provocó este error.
     */
    public CarritoVacioException(String message, Throwable cause) {
        super(message, cause);
    }
}
