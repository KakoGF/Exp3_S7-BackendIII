package cl.duoc.bancoxyz.ms.cuentas.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.bancoxyz.ms.cuentas.dtos.SolicitudMovimientoDTO;
import cl.duoc.bancoxyz.ms.cuentas.dtos.SolicitudMovimientoRequest;
import cl.duoc.bancoxyz.ms.cuentas.entities.CuentaEntity;
import cl.duoc.bancoxyz.ms.cuentas.entities.EstadoMovimiento;
import cl.duoc.bancoxyz.ms.cuentas.entities.MovimientoEntity;
import cl.duoc.bancoxyz.ms.cuentas.events.MovimientoSolicitadoEvent;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.CuentaNoEncontradaException;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.SaldoInsuficienteException;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.SolicitudInvalidaException;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.SolicitudNoEncontradaException;
import cl.duoc.bancoxyz.ms.cuentas.messaging.EventoPublisher;
import cl.duoc.bancoxyz.ms.cuentas.repositories.CuentaRepository;
import cl.duoc.bancoxyz.ms.cuentas.repositories.MovimientoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MovimientoService {

    public static final String DEPOSITO = "DEPOSITO";
    public static final String RETIRO = "RETIRO";
    private static final Set<String> TIPOS_VALIDOS = Set.of(DEPOSITO, RETIRO);
    private static final Set<String> CANALES_VALIDOS = Set.of("WEB", "MOVIL", "CAJERO");

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final EventoPublisher eventoPublisher;
    private final String topicoSolicitados;

    public MovimientoService(
            CuentaRepository cuentaRepository,
            MovimientoRepository movimientoRepository,
            EventoPublisher eventoPublisher,
            @Value("${topicos.movimientos-solicitados}") String topicoSolicitados) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.eventoPublisher = eventoPublisher;
        this.topicoSolicitados = topicoSolicitados;
    }

    @Transactional(rollbackFor = Exception.class)
    public SolicitudMovimientoDTO solicitar(SolicitudMovimientoRequest solicitud) throws Exception {
        validar(solicitud);
        String tipo = solicitud.tipoMovimiento().trim().toUpperCase(Locale.ROOT);
        String canal = solicitud.canal().trim().toUpperCase(Locale.ROOT);

        CuentaEntity cuenta = cuentaRepository.findById(solicitud.cuentaId())
                .orElseThrow(() -> new CuentaNoEncontradaException(solicitud.cuentaId()));

        if (RETIRO.equals(tipo)) {
            if (cuenta.getSaldo().compareTo(solicitud.monto()) < 0) {
                throw new SaldoInsuficienteException(cuenta.getCuentaId(), cuenta.getSaldo(), solicitud.monto());
            }
            cuenta.setSaldo(cuenta.getSaldo().subtract(solicitud.monto()));
            cuentaRepository.save(cuenta);
            log.info("[SAGA] Retencion de {} en la cuenta {} mientras se valida el retiro (saldo disponible {})",
                    solicitud.monto(), cuenta.getCuentaId(), cuenta.getSaldo());
        }

        String descripcion = solicitud.descripcion() == null || solicitud.descripcion().isBlank()
                ? "Solicitud desde canal " + canal
                : solicitud.descripcion().trim();
        String solicitudId = UUID.randomUUID().toString();

        MovimientoEntity movimiento = new MovimientoEntity(
                null, cuenta.getCuentaId(), LocalDate.now(), tipo, solicitud.monto(), descripcion,
                EstadoMovimiento.PENDIENTE, canal, solicitudId, null, LocalDateTime.now(), null);
        movimiento = movimientoRepository.save(movimiento);

        MovimientoSolicitadoEvent evento = new MovimientoSolicitadoEvent(
                UUID.randomUUID().toString(), "MOVIMIENTO_SOLICITADO", Instant.now(),
                solicitudId, movimiento.getId(), cuenta.getCuentaId(), tipo, solicitud.monto(), canal,
                cuenta.getTipo(), cuenta.getSaldo());
        eventoPublisher.publicar(topicoSolicitados, String.valueOf(cuenta.getCuentaId()), evento);

        log.info("[SAGA] Paso 1: solicitud {} ({} {} en cuenta {}, canal {}) registrada PENDIENTE",
                solicitudId, tipo, solicitud.monto(), cuenta.getCuentaId(), canal);

        return aDTO(movimiento);
    }

    @Transactional(readOnly = true)
    public SolicitudMovimientoDTO obtenerSolicitud(String solicitudId) {
        return movimientoRepository.findBySolicitudId(solicitudId)
                .map(this::aDTO)
                .orElseThrow(() -> new SolicitudNoEncontradaException(solicitudId));
    }

    @Transactional(readOnly = true)
    public List<SolicitudMovimientoDTO> listarSolicitudes(Long cuentaId) {
        if (!cuentaRepository.existsById(cuentaId)) {
            throw new CuentaNoEncontradaException(cuentaId);
        }
        return movimientoRepository.findByCuentaIdAndSolicitudIdIsNotNullOrderByIdDesc(cuentaId).stream()
                .map(this::aDTO)
                .toList();
    }

    private void validar(SolicitudMovimientoRequest solicitud) {
        if (solicitud == null || solicitud.cuentaId() == null) {
            throw new SolicitudInvalidaException("Falta el cuentaId");
        }
        if (solicitud.tipoMovimiento() == null
                || !TIPOS_VALIDOS.contains(solicitud.tipoMovimiento().trim().toUpperCase(Locale.ROOT))) {
            throw new SolicitudInvalidaException("tipoMovimiento debe ser DEPOSITO o RETIRO");
        }
        if (solicitud.monto() == null || solicitud.monto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudInvalidaException("El monto debe ser mayor que cero");
        }
        if (solicitud.canal() == null || !CANALES_VALIDOS.contains(solicitud.canal().trim().toUpperCase(Locale.ROOT))) {
            throw new SolicitudInvalidaException("canal debe ser WEB, MOVIL o CAJERO");
        }
    }

    private SolicitudMovimientoDTO aDTO(MovimientoEntity entity) {
        return new SolicitudMovimientoDTO(
                entity.getSolicitudId(), entity.getId(), entity.getCuentaId(), entity.getTipoMovimiento(),
                entity.getMonto(), entity.getCanal(), entity.getEstado().name(), entity.getMotivo(),
                entity.getFechaRegistro(), entity.getFechaResolucion());
    }
}
