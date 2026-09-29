package cl.duoc.bancoxyz.bff.cajero.dtos.core;

import java.math.BigDecimal;

public record CuentaCoreDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo) {
}
