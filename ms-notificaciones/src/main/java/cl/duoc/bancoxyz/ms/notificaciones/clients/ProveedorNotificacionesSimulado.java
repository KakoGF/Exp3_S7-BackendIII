package cl.duoc.bancoxyz.ms.notificaciones.clients;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

import cl.duoc.bancoxyz.ms.notificaciones.exceptions.ProveedorNoDisponibleException;
import cl.duoc.bancoxyz.ms.notificaciones.models.Notificacion;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProveedorNotificacionesSimulado {

    private final AtomicBoolean fallando = new AtomicBoolean(false);

    public void entregar(Notificacion notificacion) {
        if (fallando.get()) {
            throw new ProveedorNoDisponibleException("El proveedor de correo/SMS no responde (falla simulada)");
        }
        log.info("[PROVEEDOR] Mensaje entregado a {} (cuenta {}, canal {}): {}",
                notificacion.getNombreCliente(), notificacion.getCuentaId(),
                notificacion.getCanal(), notificacion.getMensaje());
    }

    public void simularFalla(boolean activa) {
        fallando.set(activa);
        log.warn("[PROVEEDOR] Falla simulada {}", activa ? "ACTIVADA" : "DESACTIVADA");
    }

    public boolean estaFallando() {
        return fallando.get();
    }
}
