package cl.duoc.bancoxyz.ms.notificaciones.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.bancoxyz.ms.notificaciones.events.MovimientoFinalizadoEvent;
import cl.duoc.bancoxyz.ms.notificaciones.exceptions.EventoInvalidoException;
import cl.duoc.bancoxyz.ms.notificaciones.services.NotificacionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovimientoFinalizadoListener {

    private final ObjectMapper objectMapper;
    private final NotificacionService notificacionService;

    @KafkaListener(id = "listenerMovimientosFinalizados", groupId = "${spring.application.name}",
            topics = "${topicos.movimientos-finalizados}")
    public void recibir(ConsumerRecord<String, String> registro) {
        log.info("[KAFKA] Recibido evento en {}-{}@{} (clave {})",
                registro.topic(), registro.partition(), registro.offset(), registro.key());
        notificacionService.notificar(leer(registro.value()));
    }

    private MovimientoFinalizadoEvent leer(String json) {
        MovimientoFinalizadoEvent evento;
        try {
            evento = objectMapper.readValue(json, MovimientoFinalizadoEvent.class);
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new EventoInvalidoException("El mensaje no es un MovimientoFinalizadoEvent valido", ex);
        }
        if (evento == null || evento.eventId() == null || evento.estado() == null
                || evento.monto() == null || evento.tipoMovimiento() == null) {
            throw new EventoInvalidoException("Al evento le faltan campos obligatorios");
        }
        return evento;
    }
}
