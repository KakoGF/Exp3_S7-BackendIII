package cl.duoc.bancoxyz.bff.cajero.clients;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.duoc.bancoxyz.bff.cajero.dtos.core.CuentaCoreDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.core.MovimientoCoreDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.core.SolicitudMovimientoCoreDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.core.SolicitudMovimientoCoreRequest;
import cl.duoc.bancoxyz.bff.cajero.exceptions.CuentaNoEncontradaException;
import cl.duoc.bancoxyz.bff.cajero.exceptions.ServicioNoDisponibleException;
import cl.duoc.bancoxyz.bff.cajero.exceptions.SolicitudRechazadaException;

@Component
public class MsCuentasClient {

    private final RestClient msCuentasRestClient;
    private final LoadBalancerClient loadBalancerClient;
    private final String serviceId;
    private final ObjectMapper objectMapper;

    public MsCuentasClient(
            RestClient msCuentasRestClient,
            LoadBalancerClient loadBalancerClient,
            @Value("${ms-cuentas.service-id}") String serviceId,
            ObjectMapper objectMapper) {
        this.msCuentasRestClient = msCuentasRestClient;
        this.loadBalancerClient = loadBalancerClient;
        this.serviceId = serviceId;
        this.objectMapper = objectMapper;
    }

    public CuentaCoreDTO obtenerCuenta(Long cuentaId) {
        try {
            return msCuentasRestClient.get()
                    .uri(resolverBaseUrl() + "/core/cuentas/{id}", cuentaId)
                    .retrieve()
                    .body(CuentaCoreDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CuentaNoEncontradaException(cuentaId);
        }
    }

    public List<MovimientoCoreDTO> obtenerMovimientos(Long cuentaId) {
        try {
            return msCuentasRestClient.get()
                    .uri(resolverBaseUrl() + "/core/cuentas/{id}/movimientos", cuentaId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<MovimientoCoreDTO>>() {
                    });
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CuentaNoEncontradaException(cuentaId);
        }
    }

    public SolicitudMovimientoCoreDTO solicitarMovimiento(SolicitudMovimientoCoreRequest solicitud) {
        try {
            return msCuentasRestClient.post()
                    .uri(resolverBaseUrl() + "/core/movimientos")
                    .body(solicitud)
                    .retrieve()
                    .body(SolicitudMovimientoCoreDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CuentaNoEncontradaException(solicitud.cuentaId());
        } catch (HttpClientErrorException ex) {
            throw new SolicitudRechazadaException(ex.getStatusCode().value(), extraerMensaje(ex));
        }
    }

    public SolicitudMovimientoCoreDTO obtenerSolicitud(String solicitudId) {
        try {
            return msCuentasRestClient.get()
                    .uri(resolverBaseUrl() + "/core/movimientos/solicitudes/{id}", solicitudId)
                    .retrieve()
                    .body(SolicitudMovimientoCoreDTO.class);
        } catch (HttpClientErrorException ex) {
            throw new SolicitudRechazadaException(ex.getStatusCode().value(), extraerMensaje(ex));
        }
    }

    private String extraerMensaje(HttpClientErrorException ex) {
        try {
            JsonNode cuerpo = objectMapper.readTree(ex.getResponseBodyAsString());
            if (cuerpo != null && cuerpo.hasNonNull("mensaje")) {
                return cuerpo.get("mensaje").asText();
            }
        } catch (Exception ignorada) {
            return ex.getStatusText();
        }
        return ex.getStatusText();
    }

    private String resolverBaseUrl() {
        ServiceInstance instancia = loadBalancerClient.choose(serviceId);
        if (instancia == null) {
            throw new ServicioNoDisponibleException(serviceId);
        }
        return instancia.getUri().toString();
    }
}
