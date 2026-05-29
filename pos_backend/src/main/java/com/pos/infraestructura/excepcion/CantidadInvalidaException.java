package com.pos.infraestructura.excepcion;

/**
 * Excepción lanzada cuando se proporciona una cantidad no positiva (≤ 0)
 * al agregar o actualizar un ítem en el carrito.
 * Corresponde a una respuesta HTTP 400 Bad Request.
 *
 * <p>Requerimientos: 5.3</p>
 */
public class CantidadInvalidaException extends RuntimeException {

    /**
     * Crea una nueva excepción con el mensaje descriptivo indicado.
     *
     * @param message descripción del error.
     */
    public CantidadInvalidaException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción con mensaje descriptivo y causa raíz.
     *
     * @param message descripción del error.
     * @param cause   excepción original que provocó este error.
     */
    public CantidadInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
