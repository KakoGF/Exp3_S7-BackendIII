package cl.duoc.bancoxyz.ms.cuentas.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.bancoxyz.ms.cuentas.events.MovimientoValidadoEvent;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.EventoInvalidoException;
import cl.duoc.bancoxyz.ms.cuentas.services.SagaMovimientoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovimientoValidadoListener {

    private final ObjectMapper objectMapper;
    private final SagaMovimientoService sagaMovimientoService;

    @KafkaListener(id = "listenerMovimientosValidados", groupId = "${spring.application.name}",
            topics = "${topicos.movimientos-validados}")
    public void recibir(ConsumerRecord<String, String> registro) throws Exception {
        log.info("[KAFKA] Recibido evento en {}-{}@{} (clave {})",
                registro.topic(), registro.partition(), registro.offset(), registro.key());
        MovimientoValidadoEvent evento = leer(registro.value());
        sagaMovimientoService.procesarValidacion(evento);
    }

    private MovimientoValidadoEvent leer(String json) {
        MovimientoValidadoEvent evento;
        try {
            evento = objectMapper.readValue(json, MovimientoValidadoEvent.class);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new EventoInvalidoException("El mensaje no es un MovimientoValidadoEvent valido", ex);
        }
        if (evento == null || evento.eventId() == null || evento.movimientoId() == null || evento.resultado() == null) {
            throw new EventoInvalidoException("Al evento le faltan campos obligatorios (eventId, movimientoId, resultado)");
        }
        return evento;
    }
}
