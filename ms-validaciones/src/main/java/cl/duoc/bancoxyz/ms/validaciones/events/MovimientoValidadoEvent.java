package cl.duoc.bancoxyz.ms.validaciones.events;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimientoValidadoEvent(
        String eventId,
        String tipoEvento,
        Instant fechaEvento,
        String solicitudId,
        Long movimientoId,
        Long cuentaId,
        String tipoMovimiento,
        BigDecimal monto,
        String canal,
        String resultado,
        String motivo,
        String validadoPor) {
}
