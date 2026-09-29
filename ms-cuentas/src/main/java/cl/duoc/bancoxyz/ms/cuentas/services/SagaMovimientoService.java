package cl.duoc.bancoxyz.ms.cuentas.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.bancoxyz.ms.cuentas.entities.CuentaEntity;
import cl.duoc.bancoxyz.ms.cuentas.entities.EstadoMovimiento;
import cl.duoc.bancoxyz.ms.cuentas.entities.EventoProcesadoEntity;
import cl.duoc.bancoxyz.ms.cuentas.entities.MovimientoEntity;
import cl.duoc.bancoxyz.ms.cuentas.events.MovimientoFinalizadoEvent;
import cl.duoc.bancoxyz.ms.cuentas.events.MovimientoValidadoEvent;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.CuentaNoEncontradaException;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.MovimientoNoEncontradoException;
import cl.duoc.bancoxyz.ms.cuentas.messaging.EventoPublisher;
import cl.duoc.bancoxyz.ms.cuentas.repositories.CuentaRepository;
import cl.duoc.bancoxyz.ms.cuentas.repositories.EventoProcesadoRepository;
import cl.duoc.bancoxyz.ms.cuentas.repositories.MovimientoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SagaMovimientoService {

    private static final String APROBADO = "APROBADO";

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final EventoProcesadoRepository eventoProcesadoRepository;
    private final EventoPublisher eventoPublisher;
    private final String topicoFinalizados;

    public SagaMovimientoService(
            CuentaRepository cuentaRepository,
            MovimientoRepository movimientoRepository,
            EventoProcesadoRepository eventoProcesadoRepository,
            EventoPublisher eventoPublisher,
            @Value("${topicos.movimientos-finalizados}") String topicoFinalizados) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
        this.eventoPublisher = eventoPublisher;
        this.topicoFinalizados = topicoFinalizados;
    }

    @Transactional(rollbackFor = Exception.class)
    public void procesarValidacion(MovimientoValidadoEvent evento) throws Exception {
        if (eventoProcesadoRepository.existsById(evento.eventId())) {
            log.warn("[DUPLICADO] Evento {} ya procesado antes: se descarta", evento.eventId());
            return;
        }

        MovimientoEntity movimiento = movimientoRepository.findById(evento.movimientoId())
                .orElseThrow(() -> new MovimientoNoEncontradoException(evento.movimientoId()));

        if (movimiento.getEstado() != EstadoMovimiento.PENDIENTE) {
            log.warn("[DUPLICADO] La solicitud {} ya estaba {}: el evento {} no cambia nada",
                    movimiento.getSolicitudId(), movimiento.getEstado(), evento.eventId());
            registrarProcesado(evento);
            return;
        }

        CuentaEntity cuenta = cuentaRepository.findById(movimiento.getCuentaId())
                .orElseThrow(() -> new CuentaNoEncontradaException(movimiento.getCuentaId()));

        boolean aprobado = APROBADO.equals(evento.resultado());
        boolean esRetiro = MovimientoService.RETIRO.equals(movimiento.getTipoMovimiento());
        boolean compensado = false;

        if (aprobado) {
            if (!esRetiro) {
                cuenta.setSaldo(cuenta.getSaldo().add(movimiento.getMonto()));
            }
            movimiento.setEstado(EstadoMovimiento.APLICADO);
            log.info("[SAGA] Paso 3: solicitud {} APROBADA por {}: {} de {} aplicado, saldo de la cuenta {} = {}",
                    movimiento.getSolicitudId(), evento.validadoPor(), movimiento.getTipoMovimiento(),
                    movimiento.getMonto(), cuenta.getCuentaId(), cuenta.getSaldo());
        } else {
            if (esRetiro) {
                cuenta.setSaldo(cuenta.getSaldo().add(movimiento.getMonto()));
                compensado = true;
                log.info("[SAGA][COMPENSACION] Solicitud {} RECHAZADA ({}): se liberan {} retenidos, saldo de la cuenta {} vuelve a {}",
                        movimiento.getSolicitudId(), evento.motivo(), movimiento.getMonto(),
                        cuenta.getCuentaId(), cuenta.getSaldo());
            } else {
                log.info("[SAGA] Paso 3: solicitud {} RECHAZADA ({}): el deposito no se aplica",
                        movimiento.getSolicitudId(), evento.motivo());
            }
            movimiento.setEstado(EstadoMovimiento.RECHAZADO);
            movimiento.setMotivo(evento.motivo());
        }

        movimiento.setFechaResolucion(LocalDateTime.now());
        movimientoRepository.save(movimiento);
        cuentaRepository.save(cuenta);
        registrarProcesado(evento);

        MovimientoFinalizadoEvent finalizado = new MovimientoFinalizadoEvent(
                UUID.randomUUID().toString(), "MOVIMIENTO_FINALIZADO", Instant.now(),
                movimiento.getSolicitudId(), movimiento.getId(), cuenta.getCuentaId(), cuenta.getNombre(),
                movimiento.getTipoMovimiento(), movimiento.getMonto(), movimiento.getCanal(),
                movimiento.getEstado().name(), movimiento.getMotivo(), cuenta.getSaldo(), compensado);
        eventoPublisher.publicar(topicoFinalizados, String.valueOf(cuenta.getCuentaId()), finalizado);
    }

    private void registrarProcesado(MovimientoValidadoEvent evento) {
        eventoProcesadoRepository.save(new EventoProcesadoEntity(
                evento.eventId(), evento.tipoEvento(), LocalDateTime.now()));
    }
}
