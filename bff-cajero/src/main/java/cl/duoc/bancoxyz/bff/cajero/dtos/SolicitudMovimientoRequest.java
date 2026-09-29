package cl.duoc.bancoxyz.bff.cajero.dtos;

import java.math.BigDecimal;

public record SolicitudMovimientoRequest(
        String tipoMovimiento,
        BigDecimal monto,
        String descripcion) {
}
