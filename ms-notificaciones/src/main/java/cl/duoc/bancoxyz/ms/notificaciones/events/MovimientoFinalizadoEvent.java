package cl.duoc.bancoxyz.ms.notificaciones.events;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimientoFinalizadoEvent(
        String eventId,
        String tipoEvento,
        Instant fechaEvento,
        String solicitudId,
        Long movimientoId,
        Long cuentaId,
        String nombreCliente,
        String tipoMovimiento,
        BigDecimal monto,
        String canal,
        String estado,
        String motivo,
        BigDecimal saldoFinal,
        boolean compensado) {
}
