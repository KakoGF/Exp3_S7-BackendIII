package cl.duoc.bancoxyz.ms.cuentas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long cuentaId) {
        super("No existe la cuenta con id " + cuentaId);
    }
}
