package cl.duoc.bancoxyz.bff.web.dtos;

import java.math.BigDecimal;
import java.util.List;

public record CuentaWebDetalleDTO(
        Long cuentaId,
        String nombre,
        BigDecimal saldo,
        String tipo,
        Integer edad,
        List<MovimientoWebDTO> movimientos,
        ResumenMovimientosDTO resumen) {
}
