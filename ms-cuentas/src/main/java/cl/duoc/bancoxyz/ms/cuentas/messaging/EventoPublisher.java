package cl.duoc.bancoxyz.ms.cuentas.messaging;

import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.bancoxyz.ms.cuentas.exceptions.MensajeriaNoDisponibleException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventoPublisher {

    private static final long SEGUNDOS_ESPERA = 5;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "kafkaBroker", fallbackMethod = "publicarFallback")
    public void publicar(String topico, String clave, Object evento) throws Exception {
        String json = objectMapper.writeValueAsString(evento);
        SendResult<String, String> resultado = kafkaTemplate.send(topico, clave, json)
                .get(SEGUNDOS_ESPERA, TimeUnit.SECONDS);
        log.info("[KAFKA] Evento publicado en {} particion {} offset {} (clave {})",
                topico, resultado.getRecordMetadata().partition(),
                resultado.getRecordMetadata().offset(), clave);
    }

    @SuppressWarnings("unused")
    private void publicarFallback(String topico, String clave, Object evento, Throwable error) {
        log.warn("[FALLBACK] No se pudo publicar en {}: Kafka no esta disponible. Causa: {}", topico, error.toString());
        throw new MensajeriaNoDisponibleException("El sistema de mensajeria no esta disponible, intenta en unos minutos");
    }
}
