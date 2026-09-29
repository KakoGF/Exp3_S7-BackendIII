package cl.duoc.bancoxyz.ms.cuentas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumidorConfig {

    @Bean
    public DefaultErrorHandler manejadorErroresKafka(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kafka.consumidor.reintentos}") long reintentos,
            @Value("${kafka.consumidor.intervalo-ms}") long intervaloMs) {
        DeadLetterPublishingRecoverer enviarADlt = new DeadLetterPublishingRecoverer(kafkaTemplate);
        return new DefaultErrorHandler(enviarADlt, new FixedBackOff(intervaloMs, reintentos));
    }
}
