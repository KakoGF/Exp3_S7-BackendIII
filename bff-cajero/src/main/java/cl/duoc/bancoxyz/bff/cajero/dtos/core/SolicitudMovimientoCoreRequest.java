package cl.duoc.bancoxyz.bff.cajero.dtos.core;

import java.math.BigDecimal;

public record SolicitudMovimientoCoreRequest(
        Long cuentaId,
        String tipoMovimiento,
        BigDecimal monto,
        String canal,
        String descripcion) {
}
