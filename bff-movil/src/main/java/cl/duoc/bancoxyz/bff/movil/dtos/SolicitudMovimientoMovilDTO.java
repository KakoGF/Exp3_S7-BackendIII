package cl.duoc.bancoxyz.bff.movil.dtos;

import java.math.BigDecimal;

public record SolicitudMovimientoMovilDTO(
        String solicitudId,
        String tipoMovimiento,
        BigDecimal monto,
        String estado,
        String mensaje) {
}
