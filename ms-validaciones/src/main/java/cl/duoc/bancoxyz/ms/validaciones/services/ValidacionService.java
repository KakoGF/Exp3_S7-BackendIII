package cl.duoc.bancoxyz.ms.validaciones.services;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.bancoxyz.ms.validaciones.dtos.EstadisticasDTO;
import cl.duoc.bancoxyz.ms.validaciones.events.MovimientoSolicitadoEvent;
import cl.duoc.bancoxyz.ms.validaciones.events.MovimientoValidadoEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidacionService {

    private final ReglasValidacionService reglasValidacionService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicoValidados;
    private final String instancia;
    private final long latenciaSimuladaMs;

    private final Set<String> eventosProcesados = ConcurrentHashMap.newKeySet();
    private final Set<Integer> particionesAtendidas = ConcurrentHashMap.newKeySet();
    private final AtomicLong procesadas = new AtomicLong();
    private final AtomicLong aprobadas = new AtomicLong();
    private final AtomicLong rechazadas = new AtomicLong();
    private final AtomicLong duplicadas = new AtomicLong();

    public ValidacionService(
            ReglasValidacionService reglasValidacionService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${topicos.movimientos-validados}") String topicoValidados,
            @Value("${spring.application.name}:${server.port}") String instancia,
            @Value("${validaciones.latencia-simulada-ms}") long latenciaSimuladaMs) {
        this.reglasValidacionService = reglasValidacionService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicoValidados = topicoValidados;
        this.instancia = instancia;
        this.latenciaSimuladaMs = latenciaSimuladaMs;
    }

    public void validar(MovimientoSolicitadoEvent evento, int particion, long offset) throws Exception {
        if (eventosProcesados.contains(evento.eventId())) {
            duplicadas.incrementAndGet();
            log.warn("[DUPLICADO][{}] Evento {} ya validado antes: se descarta", instancia, evento.eventId());
            return;
        }

        Thread.sleep(latenciaSimuladaMs);
        ResultadoValidacion resultado = reglasValidacionService.evaluar(evento);

        MovimientoValidadoEvent validado = new MovimientoValidadoEvent(
                UUID.randomUUID().toString(), "MOVIMIENTO_VALIDADO", Instant.now(),
                evento.solicitudId(), evento.movimientoId(), evento.cuentaId(), evento.tipoMovimiento(),
                evento.monto(), evento.canal(),
                resultado.aprobado() ? "APROBADO" : "RECHAZADO",
                resultado.motivo(), instancia);

        String json = objectMapper.writeValueAsString(validado);
        kafkaTemplate.send(topicoValidados, String.valueOf(evento.cuentaId()), json).get(5, TimeUnit.SECONDS);

        eventosProcesados.add(evento.eventId());
        particionesAtendidas.add(particion);
        procesadas.incrementAndGet();
        if (resultado.aprobado()) {
            aprobadas.incrementAndGet();
        } else {
            rechazadas.incrementAndGet();
        }
        log.info("[SAGA] Paso 2 [{}] particion {} offset {}: solicitud {} ({} {} cuenta {}, canal {}) -> {}{}",
                instancia, particion, offset, evento.solicitudId(), evento.tipoMovimiento(), evento.monto(),
                evento.cuentaId(), evento.canal(), validado.resultado(),
                resultado.motivo() == null ? "" : " (" + resultado.motivo() + ")");
    }

    public EstadisticasDTO estadisticas() {
        List<Integer> particiones = particionesAtendidas.stream().sorted().toList();
        return new EstadisticasDTO(instancia, particiones,
                procesadas.get(), aprobadas.get(), rechazadas.get(), duplicadas.get());
    }
}
