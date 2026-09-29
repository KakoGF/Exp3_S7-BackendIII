package cl.duoc.bancoxyz.bff.movil.dtos.core;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoCoreDTO(
        Long cuentaId,
        LocalDate fecha,
        String tipoMovimiento,
        BigDecimal monto,
        String descripcion) {
}
