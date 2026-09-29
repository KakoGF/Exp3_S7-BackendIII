package cl.duoc.bancoxyz.bff.movil.dtos;

import java.math.BigDecimal;

public record CuentaMovilResumenDTO(
        Long id,
        String nombre,
        BigDecimal saldo) {
}
