package cl.duoc.bancoxyz.ms.validaciones.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.bancoxyz.ms.validaciones.events.MovimientoSolicitadoEvent;
import cl.duoc.bancoxyz.ms.validaciones.exceptions.EventoInvalidoException;
import cl.duoc.bancoxyz.ms.validaciones.services.ValidacionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovimientoSolicitadoListener {

    private final ObjectMapper objectMapper;
    private final ValidacionService validacionService;

    @KafkaListener(id = "listenerMovimientosSolicitados", groupId = "${spring.application.name}",
            topics = "${topicos.movimientos-solicitados}")
    public void recibir(ConsumerRecord<String, String> registro) throws Exception {
        log.info("[KAFKA] Recibido evento en {}-{}@{} (clave {})",
                registro.topic(), registro.partition(), registro.offset(), registro.key());
        MovimientoSolicitadoEvent evento = leer(registro.value());
        validacionService.validar(evento, registro.partition(), registro.offset());
    }

    private MovimientoSolicitadoEvent leer(String json) {
        MovimientoSolicitadoEvent evento;
        try {
            evento = objectMapper.readValue(json, MovimientoSolicitadoEvent.class);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new EventoInvalidoException("El mensaje no es un MovimientoSolicitadoEvent valido", ex);
        }
        if (evento == null || evento.eventId() == null || evento.movimientoId() == null
                || evento.cuentaId() == null || evento.tipoMovimiento() == null
                || evento.monto() == null || evento.canal() == null) {
            throw new EventoInvalidoException("Al evento le faltan campos obligatorios");
        }
        return evento;
    }
}
