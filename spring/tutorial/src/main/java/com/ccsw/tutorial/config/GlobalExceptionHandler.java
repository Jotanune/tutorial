package com.ccsw.tutorial.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para convertir las excepciones de negocio
 * en respuestas HTTP apropiadas con mensajes legibles
 *
 * @author ccsw
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja las excepciones de negocio (validaciones) y las convierte en respuestas HTTP 400
     *
     * @param ex la excepción lanzada
     * @return ResponseEntity con el mensaje de error y código 400
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());

        // Si el mensaje contiene palabras clave de validación, devolver 400 (Bad Request)
        // De lo contrario, devolver 500 (Internal Server Error)
        if (ex.getMessage() != null && (ex.getMessage().contains("fecha") || ex.getMessage().contains("préstamo") || ex.getMessage().contains("prestado") || ex.getMessage().contains("cliente") || ex.getMessage().contains("juego")
                || ex.getMessage().contains("día") || ex.getMessage().contains("Ya existe"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

