package com.pos.infraestructura.excepcion;

/**
 * Excepción lanzada cuando se solicita un producto que no existe en el catálogo.
 * Corresponde a una respuesta HTTP 404 Not Found.
 *
 * <p>Requerimientos: 5.3</p>
 */
public class ProductoNoEncontradoException extends RuntimeException {

    /**
     * Crea una nueva excepción con el mensaje descriptivo indicado.
     *
     * @param message descripción del error.
     */
    public ProductoNoEncontradoException(String message) {
        super(message);
    }

    /**
     * Crea una nueva excepción con mensaje descriptivo y causa raíz.
     *
     * @param message descripción del error.
     * @param cause   excepción original que provocó este error.
     */
    public ProductoNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
