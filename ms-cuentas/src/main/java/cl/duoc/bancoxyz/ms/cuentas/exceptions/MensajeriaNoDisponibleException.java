package cl.duoc.bancoxyz.ms.cuentas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MensajeriaNoDisponibleException extends RuntimeException {

    public MensajeriaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
