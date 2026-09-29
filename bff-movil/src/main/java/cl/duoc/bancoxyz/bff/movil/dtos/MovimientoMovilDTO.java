package cl.duoc.bancoxyz.bff.movil.dtos;

import java.math.BigDecimal;

public record MovimientoMovilDTO(
        String fecha,
        BigDecimal monto,
        String tipo) {
}
