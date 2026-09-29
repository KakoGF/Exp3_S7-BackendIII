package cl.duoc.bancoxyz.bff.cajero.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String serviceId) {
        super("No hay instancias disponibles del servicio " + serviceId + " en el registro");
    }
}
