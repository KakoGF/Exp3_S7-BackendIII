package cl.duoc.bancoxyz.ms.notificaciones.clients;

import org.springframework.stereotype.Component;

import cl.duoc.bancoxyz.ms.notificaciones.models.Notificacion;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProveedorNotificacionesClient {

    private final ProveedorNotificacionesSimulado proveedor;

    @CircuitBreaker(name = "proveedorNotificaciones", fallbackMethod = "enviarFallback")
    public boolean enviar(Notificacion notificacion) {
        proveedor.entregar(notificacion);
        return true;
    }

    @SuppressWarnings("unused")
    private boolean enviarFallback(Notificacion notificacion, Throwable error) {
        log.warn("[FALLBACK] No se pudo entregar la notificacion de la solicitud {}: queda PENDIENTE. Causa: {}",
                notificacion.getSolicitudId(), error.toString());
        return false;
    }
}
