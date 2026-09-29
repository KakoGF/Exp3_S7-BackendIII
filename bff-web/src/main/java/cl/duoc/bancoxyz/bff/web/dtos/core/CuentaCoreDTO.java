package cl.duoc.bancoxyz.bff.web.dtos.core;

import java.math.BigDecimal;

public record CuentaCoreDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        Integer edad,
        String tipo) {
}
