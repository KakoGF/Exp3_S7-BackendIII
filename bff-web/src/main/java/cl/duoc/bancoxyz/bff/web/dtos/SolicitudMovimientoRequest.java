package cl.duoc.bancoxyz.bff.web.dtos;

import java.math.BigDecimal;

public record SolicitudMovimientoRequest(
        String tipoMovimiento,
        BigDecimal monto,
        String descripcion) {
}
