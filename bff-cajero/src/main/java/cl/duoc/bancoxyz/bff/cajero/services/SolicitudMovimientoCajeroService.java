package cl.duoc.bancoxyz.bff.cajero.services;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.bff.cajero.clients.MsCuentasClient;
import cl.duoc.bancoxyz.bff.cajero.dtos.ComprobanteCajeroDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.RespuestaSolicitud;
import cl.duoc.bancoxyz.bff.cajero.dtos.SolicitudMovimientoRequest;
import cl.duoc.bancoxyz.bff.cajero.dtos.core.SolicitudMovimientoCoreDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.core.SolicitudMovimientoCoreRequest;
import cl.duoc.bancoxyz.bff.cajero.exceptions.CuentaNoEncontradaException;
import cl.duoc.bancoxyz.bff.cajero.exceptions.SolicitudRechazadaException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitudMovimientoCajeroService {

    private static final String CANAL = "CAJERO";
    private static final String NO_REGISTRADA = "NO_REGISTRADA";
    private static final String DESCONOCIDO = "DESCONOCIDO";

    private final MsCuentasClient msCuentasClient;

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "solicitarFallback")
    public RespuestaSolicitud solicitar(Long cuentaId, SolicitudMovimientoRequest solicitud) {
        log.info("[CAJERO] Enviando solicitud a ms-cuentas ({} {} en cuenta {})",
                solicitud.tipoMovimiento(), solicitud.monto(), cuentaId);
        SolicitudMovimientoCoreDTO core = msCuentasClient.solicitarMovimiento(
                new SolicitudMovimientoCoreRequest(cuentaId, solicitud.tipoMovimiento(), solicitud.monto(),
                        CANAL, solicitud.descripcion()));
        return new RespuestaSolicitud(HttpStatus.ACCEPTED, adaptar(core, mensajePara(core)));
    }

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "consultarFallback")
    public ComprobanteCajeroDTO consultar(String solicitudId) {
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
        ComprobanteCajeroDTO cuerpo = sinRespuesta(null, cuentaId, solicitud.tipoMovimiento(), solicitud.monto(),
                NO_REGISTRADA, "No se pudo registrar la solicitud en este momento y no se hizo ningun cargo. Intenta mas tarde");
        return new RespuestaSolicitud(HttpStatus.SERVICE_UNAVAILABLE, cuerpo);
    }

    @SuppressWarnings("unused")
    private ComprobanteCajeroDTO consultarFallback(String solicitudId, Throwable error) {
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

    private ComprobanteCajeroDTO adaptar(SolicitudMovimientoCoreDTO core, String mensaje) {
        return new ComprobanteCajeroDTO(core.solicitudId(), core.estado(), core.monto(), mensaje);
    }

    private ComprobanteCajeroDTO sinRespuesta(String solicitudId, Long cuentaId, String tipoMovimiento,
            BigDecimal monto, String estado, String mensaje) {
        return new ComprobanteCajeroDTO(solicitudId, estado, monto, mensaje);
    }
}
