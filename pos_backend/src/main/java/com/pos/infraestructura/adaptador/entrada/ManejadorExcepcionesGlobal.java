package com.pos.infraestructura.adaptador.entrada;

import com.pos.infraestructura.excepcion.CantidadInvalidaException;
import com.pos.infraestructura.excepcion.CarritoVacioException;
import com.pos.infraestructura.excepcion.ProductoNoEncontradoException;
import com.pos.infraestructura.excepcion.VentaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la API REST.
 * Centraliza el manejo de errores y produce respuestas JSON uniformes.
 *
 * <p>Requerimientos: 5.3, 5.5, 5.6, 6.3</p>
 */
@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    /**
     * Maneja excepciones de producto no encontrado → HTTP 404.
     */
    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarProductoNoEncontrado(ProductoNoEncontradoException ex) {
        return construirRespuesta(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones de venta no encontrada → HTTP 404.
     */
    @ExceptionHandler(VentaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> manejarVentaNoEncontrada(VentaNoEncontradaException ex) {
        return construirRespuesta(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja excepciones de carrito vacío → HTTP 400.
     */
    @ExceptionHandler(CarritoVacioException.class)
    public ResponseEntity<Map<String, Object>> manejarCarritoVacio(CarritoVacioException ex) {
        return construirRespuesta(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja excepciones de cantidad inválida → HTTP 400.
     */
    @ExceptionHandler(CantidadInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarCantidadInvalida(CantidadInvalidaException ex) {
        return construirRespuesta(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de validación de Bean Validation → HTTP 400.
     * El campo "error" contiene los mensajes de los campos inválidos concatenados.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> mensajes = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        String mensajeError = String.join("; ", mensajes);
        return construirRespuesta(mensajeError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja cualquier excepción no controlada → HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionGeneral(Exception ex) {
        return construirRespuesta("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Construye el cuerpo de respuesta estándar con "error", "timestamp" y "status".
     */
    private ResponseEntity<Map<String, Object>> construirRespuesta(String mensajeError, HttpStatus status) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("error", mensajeError);
        cuerpo.put("timestamp", LocalDateTime.now().toString());
        cuerpo.put("status", status.value());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
