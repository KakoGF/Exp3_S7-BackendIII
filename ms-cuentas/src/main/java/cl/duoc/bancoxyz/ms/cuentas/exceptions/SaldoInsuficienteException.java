package cl.duoc.bancoxyz.ms.cuentas.exceptions;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(Long cuentaId, BigDecimal saldo, BigDecimal monto) {
        super("La cuenta " + cuentaId + " tiene saldo " + saldo + " y no alcanza para retirar " + monto);
    }
}
