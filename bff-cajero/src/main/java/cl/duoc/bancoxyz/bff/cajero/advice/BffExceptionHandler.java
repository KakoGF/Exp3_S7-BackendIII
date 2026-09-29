package cl.duoc.bancoxyz.bff.cajero.advice;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.bancoxyz.bff.cajero.exceptions.SolicitudRechazadaException;

@RestControllerAdvice
public class BffExceptionHandler {

    @ExceptionHandler(SolicitudRechazadaException.class)
    public ResponseEntity<Map<String, Object>> manejarSolicitudRechazada(SolicitudRechazadaException ex) {
        return ResponseEntity.status(ex.getEstadoHttp())
                .body(Map.of("estado", ex.getEstadoHttp(), "mensaje", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("estado", 400, "mensaje", "El cuerpo de la solicitud no es un JSON valido"));
    }
}
