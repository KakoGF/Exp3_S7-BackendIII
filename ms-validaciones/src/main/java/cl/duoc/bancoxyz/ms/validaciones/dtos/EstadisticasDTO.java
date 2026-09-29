package cl.duoc.bancoxyz.ms.validaciones.dtos;

import java.util.List;

public record EstadisticasDTO(
        String instancia,
        List<Integer> particionesAtendidas,
        long solicitudesProcesadas,
        long aprobadas,
        long rechazadas,
        long duplicadasDescartadas) {
}
