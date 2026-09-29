package cl.duoc.bancoxyz.bff.cajero.dtos.core;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitudMovimientoCoreDTO(
        String solicitudId,
        Long movimientoId,
        Long cuentaId,
        String tipoMovimiento,
        BigDecimal monto,
        String canal,
        String estado,
        String motivo,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaResolucion) {
}
