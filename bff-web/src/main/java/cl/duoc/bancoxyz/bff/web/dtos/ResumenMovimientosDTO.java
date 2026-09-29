package cl.duoc.bancoxyz.bff.web.dtos;

import java.math.BigDecimal;

public record ResumenMovimientosDTO(
        BigDecimal totalDepositos,
        BigDecimal totalRetiros,
        BigDecimal totalCompras,
        BigDecimal totalPagos,
        BigDecimal saldoNetoMovimientos,
        int cantidadMovimientos) {
}
