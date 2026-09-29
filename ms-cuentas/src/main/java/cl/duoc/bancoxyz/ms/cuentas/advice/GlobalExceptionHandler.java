package cl.duoc.bancoxyz.ms.cuentas.advice;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.bancoxyz.ms.cuentas.exceptions.CuentaNoEncontradaException;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.MensajeriaNoDisponibleException;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.SaldoInsuficienteException;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.SolicitudInvalidaException;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.SolicitudNoEncontradaException;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Map<String, String>> manejarLimiteExcedido(RequestNotPermitted ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of(
                        "error", "Demasiadas solicitudes",
                        "mensaje", "El servicio esta recibiendo mas solicitudes de las permitidas, reintenta en unos segundos"));
    }

    @ExceptionHandler({CuentaNoEncontradaException.class, SolicitudNoEncontradaException.class})
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(RuntimeException ex) {
        return cuerpo(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage());
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Map<String, String>> manejarSolicitudInvalida(SolicitudInvalidaException ex) {
        return cuerpo(HttpStatus.BAD_REQUEST, "Solicitud invalida", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        return cuerpo(HttpStatus.BAD_REQUEST, "Solicitud invalida", "El cuerpo de la solicitud no es un JSON valido");
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, String>> manejarSaldoInsuficiente(SaldoInsuficienteException ex) {
        return cuerpo(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo insuficiente", ex.getMessage());
    }

    @ExceptionHandler(MensajeriaNoDisponibleException.class)
    public ResponseEntity<Map<String, String>> manejarMensajeriaNoDisponible(MensajeriaNoDisponibleException ex) {
        return cuerpo(HttpStatus.SERVICE_UNAVAILABLE, "Mensajeria no disponible", ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> cuerpo(HttpStatus estado, String error, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("error", error, "mensaje", mensaje));
    }
}
