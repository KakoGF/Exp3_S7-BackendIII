package cl.duoc.bancoxyz.ms.cuentas.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SolicitudMovimientoDTO(
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
