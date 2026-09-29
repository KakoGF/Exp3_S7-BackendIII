package cl.duoc.bancoxyz.bff.web.dtos;

import java.math.BigDecimal;

public record CuentaWebResumenDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        String tipo,
        Integer edad) {
}
