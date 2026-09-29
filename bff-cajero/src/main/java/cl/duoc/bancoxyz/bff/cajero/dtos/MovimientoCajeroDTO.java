package cl.duoc.bancoxyz.bff.cajero.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoCajeroDTO(
        LocalDate fecha,
        String tipoMovimiento,
        BigDecimal monto) {
}
