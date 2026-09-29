package cl.duoc.bancoxyz.bff.movil.dtos;

import java.math.BigDecimal;
import java.util.List;

public record CuentaMovilDetalleDTO(
        Long id,
        String nombre,
        BigDecimal saldo,
        List<MovimientoMovilDTO> ultimosMovimientos) {
}
