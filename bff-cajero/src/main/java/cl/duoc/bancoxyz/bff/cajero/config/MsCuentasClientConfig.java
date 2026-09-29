package cl.duoc.bancoxyz.bff.cajero.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class MsCuentasClientConfig {

    @Bean
    public RestClient msCuentasRestClient(
            @Value("${ms-cuentas.timeout.conexion-ms}") long timeoutConexionMs,
            @Value("${ms-cuentas.timeout.lectura-ms}") long timeoutLecturaMs,
            @Value("${ms-cuentas.credenciales.usuario}") String usuarioServicio,
            @Value("${ms-cuentas.credenciales.clave}") String claveServicio) {

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofMillis(timeoutConexionMs))
                .withReadTimeout(Duration.ofMillis(timeoutLecturaMs));

        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeaders(headers -> headers.setBasicAuth(usuarioServicio, claveServicio))
                .build();
    }
}
