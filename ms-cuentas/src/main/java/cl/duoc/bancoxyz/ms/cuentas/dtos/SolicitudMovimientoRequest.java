package cl.duoc.bancoxyz.ms.cuentas.dtos;

import java.math.BigDecimal;

public record SolicitudMovimientoRequest(
        Long cuentaId,
        String tipoMovimiento,
        BigDecimal monto,
        String canal,
        String descripcion) {
}
