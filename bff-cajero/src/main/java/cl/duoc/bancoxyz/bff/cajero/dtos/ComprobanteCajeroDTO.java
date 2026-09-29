package cl.duoc.bancoxyz.bff.cajero.dtos;

import java.math.BigDecimal;

public record ComprobanteCajeroDTO(
        String solicitudId,
        String estado,
        BigDecimal monto,
        String mensaje) {
}
