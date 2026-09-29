package cl.duoc.bancoxyz.bff.web.services;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.bff.web.clients.MsCuentasClient;
import cl.duoc.bancoxyz.bff.web.dtos.SolicitudMovimientoWebDTO;
import cl.duoc.bancoxyz.bff.web.dtos.RespuestaSolicitud;
import cl.duoc.bancoxyz.bff.web.dtos.SolicitudMovimientoRequest;
import cl.duoc.bancoxyz.bff.web.dtos.core.SolicitudMovimientoCoreDTO;
import cl.duoc.bancoxyz.bff.web.dtos.core.SolicitudMovimientoCoreRequest;
import cl.duoc.bancoxyz.bff.web.exceptions.CuentaNoEncontradaException;
import cl.duoc.bancoxyz.bff.web.exceptions.SolicitudRechazadaException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudMovimientoWebService {

    private static final String CANAL = "WEB";
    private static final String NO_REGISTRADA = "NO_REGISTRADA";
    private static final String DESCONOCIDO = "DESCONOCIDO";

    private final MsCuentasClient msCuentasClient;

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "solicitarFallback")
    public RespuestaSolicitud solicitar(Long cuentaId, SolicitudMovimientoRequest solicitud) {
        log.info("[WEB] Enviando solicitud a ms-cuentas ({} {} en cuenta {})",
                solicitud.tipoMovimiento(), solicitud.monto(), cuentaId);
        SolicitudMovimientoCoreDTO core = msCuentasClient.solicitarMovimiento(
                new SolicitudMovimientoCoreRequest(cuentaId, solicitud.tipoMovimiento(), solicitud.monto(),
                        CANAL, solicitud.descripcion()));
        return new RespuestaSolicitud(HttpStatus.ACCEPTED, adaptar(core, mensajePara(core)));
    }

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "consultarFallback")
    public SolicitudMovimientoWebDTO consultar(String solicitudId) {
        SolicitudMovimientoCoreDTO core = msCuentasClient.obtenerSolicitud(solicitudId);
        return adaptar(core, mensajePara(core));
    }

    @SuppressWarnings("unused")
    private RespuestaSolicitud solicitarFallback(Long cuentaId, SolicitudMovimientoRequest solicitud, Throwable error) {
        if (error instanceof CuentaNoEncontradaException noEncontrada) {
            throw noEncontrada;
        }
        if (error instanceof SolicitudRechazadaException rechazada) {
            throw rechazada;
        }
        log.warn("[FALLBACK] Solicitud NO registrada para la cuenta {}. Causa: {}", cuentaId, error.toString());
        SolicitudMovimientoWebDTO cuerpo = sinRespuesta(null, cuentaId, solicitud.tipoMovimiento(), solicitud.monto(),
                NO_REGISTRADA, "No se pudo registrar la solicitud en este momento y no se hizo ningun cargo. Intenta mas tarde");
        return new RespuestaSolicitud(HttpStatus.SERVICE_UNAVAILABLE, cuerpo);
    }

    @SuppressWarnings("unused")
    private SolicitudMovimientoWebDTO consultarFallback(String solicitudId, Throwable error) {
        if (error instanceof SolicitudRechazadaException rechazada) {
            throw rechazada;
        }
        log.warn("[FALLBACK] No se pudo consultar la solicitud {}. Causa: {}", solicitudId, error.toString());
        return sinRespuesta(solicitudId, null, null, null, DESCONOCIDO,
                "No se pudo consultar el estado en este momento, intenta mas tarde");
    }

    private String mensajePara(SolicitudMovimientoCoreDTO core) {
        return switch (core.estado()) {
            case "PENDIENTE" -> "Solicitud recibida, se esta validando";
            case "APLICADO" -> "Operacion aplicada";
            case "RECHAZADO" -> "Operacion rechazada: " + core.motivo();
            default -> core.estado();
        };
    }

    private SolicitudMovimientoWebDTO adaptar(SolicitudMovimientoCoreDTO core, String mensaje) {
        return new SolicitudMovimientoWebDTO(
                core.solicitudId(), core.cuentaId(), core.tipoMovimiento(), core.monto(), core.estado(),
                core.motivo(), mensaje, core.fechaRegistro(), core.fechaResolucion());
    }

    private SolicitudMovimientoWebDTO sinRespuesta(String solicitudId, Long cuentaId, String tipoMovimiento,
            BigDecimal monto, String estado, String mensaje) {
        return new SolicitudMovimientoWebDTO(
                solicitudId, cuentaId, tipoMovimiento, monto, estado, null, mensaje, null, null);
    }
}
