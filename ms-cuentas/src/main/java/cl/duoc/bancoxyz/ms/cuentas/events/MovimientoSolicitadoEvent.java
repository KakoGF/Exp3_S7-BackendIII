package cl.duoc.bancoxyz.ms.cuentas.events;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimientoSolicitadoEvent(
        String eventId,
        String tipoEvento,
        Instant fechaEvento,
        String solicitudId,
        Long movimientoId,
        Long cuentaId,
        String tipoMovimiento,
        BigDecimal monto,
        String canal,
        String tipoCuenta,
        BigDecimal saldoDisponible) {
}
