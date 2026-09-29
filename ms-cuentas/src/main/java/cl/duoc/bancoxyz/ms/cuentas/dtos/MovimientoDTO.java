package cl.duoc.bancoxyz.ms.cuentas.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoDTO(
        Long cuentaId,
        LocalDate fecha,
        String tipoMovimiento,
        BigDecimal monto,
        String descripcion) {
}
