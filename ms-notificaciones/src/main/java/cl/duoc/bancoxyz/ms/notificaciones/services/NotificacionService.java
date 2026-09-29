package cl.duoc.bancoxyz.ms.notificaciones.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.ms.notificaciones.clients.ProveedorNotificacionesClient;
import cl.duoc.bancoxyz.ms.notificaciones.events.MovimientoFinalizadoEvent;
import cl.duoc.bancoxyz.ms.notificaciones.models.EstadoNotificacion;
import cl.duoc.bancoxyz.ms.notificaciones.models.Notificacion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final ProveedorNotificacionesClient proveedorClient;

    private final List<Notificacion> notificaciones = new CopyOnWriteArrayList<>();
    private final Set<String> eventosProcesados = ConcurrentHashMap.newKeySet();

    public void notificar(MovimientoFinalizadoEvent evento) {
        if (!eventosProcesados.add(evento.eventId())) {
            log.warn("[DUPLICADO] Evento {} ya notificado: se descarta", evento.eventId());
            return;
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setNotificacionId(UUID.randomUUID().toString());
        notificacion.setEventId(evento.eventId());
        notificacion.setSolicitudId(evento.solicitudId());
        notificacion.setCuentaId(evento.cuentaId());
        notificacion.setNombreCliente(evento.nombreCliente());
        notificacion.setCanal(evento.canal());
        notificacion.setMensaje(redactar(evento));
        notificacion.setFechaCreacion(LocalDateTime.now());

        boolean enviada = proveedorClient.enviar(notificacion);
        if (enviada) {
            notificacion.setEstado(EstadoNotificacion.ENVIADA);
            notificacion.setFechaEnvio(LocalDateTime.now());
        } else {
            notificacion.setEstado(EstadoNotificacion.PENDIENTE);
        }
        notificaciones.add(0, notificacion);

        log.info("[SAGA] Paso 4: solicitud {} {} notificada al cliente -> {}",
                evento.solicitudId(), evento.estado(), notificacion.getEstado());
    }

    public List<Notificacion> listarNotificaciones() {
        return new ArrayList<>(notificaciones);
    }

    private String redactar(MovimientoFinalizadoEvent evento) {
        String operacion = "RETIRO".equals(evento.tipoMovimiento()) ? "retiro" : "deposito";
        if ("APLICADO".equals(evento.estado())) {
            return "Hola " + evento.nombreCliente() + ", tu " + operacion + " de $" + evento.monto().toPlainString()
                    + " por canal " + evento.canal() + " fue aplicado. Saldo actual: $" + evento.saldoFinal().toPlainString();
        }
        String complemento = evento.compensado()
                ? " El monto retenido fue devuelto a tu cuenta."
                : "";
        return "Hola " + evento.nombreCliente() + ", tu " + operacion + " de $" + evento.monto().toPlainString()
                + " por canal " + evento.canal() + " fue rechazado (" + evento.motivo() + ")." + complemento;
    }
}
