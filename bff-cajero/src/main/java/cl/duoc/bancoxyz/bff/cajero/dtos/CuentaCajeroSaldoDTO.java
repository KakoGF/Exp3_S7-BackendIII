package cl.duoc.bancoxyz.bff.cajero.dtos;

import java.math.BigDecimal;

public record CuentaCajeroSaldoDTO(
        Long cuentaId,
        BigDecimal saldo,
        String tipo) {
}
