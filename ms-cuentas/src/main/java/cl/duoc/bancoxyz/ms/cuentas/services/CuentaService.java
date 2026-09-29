package cl.duoc.bancoxyz.ms.cuentas.services;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.ms.cuentas.dtos.CuentaDTO;
import cl.duoc.bancoxyz.ms.cuentas.dtos.MovimientoDTO;
import cl.duoc.bancoxyz.ms.cuentas.entities.CuentaEntity;
import cl.duoc.bancoxyz.ms.cuentas.entities.EstadoMovimiento;
import cl.duoc.bancoxyz.ms.cuentas.entities.MovimientoEntity;
import cl.duoc.bancoxyz.ms.cuentas.exceptions.CuentaNoEncontradaException;
import cl.duoc.bancoxyz.ms.cuentas.repositories.CuentaRepository;
import cl.duoc.bancoxyz.ms.cuentas.repositories.MovimientoRepository;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    @RateLimiter(name = "msCuentasApi")
    public List<CuentaDTO> listarCuentas() {
        return cuentaRepository.findAll().stream()
                .map(this::aDTO)
                .toList();
    }

    public CuentaDTO obtenerCuenta(Long cuentaId) {
        CuentaEntity cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new CuentaNoEncontradaException(cuentaId));
        return aDTO(cuenta);
    }

    public List<MovimientoDTO> listarMovimientos(Long cuentaId) {
        if (!cuentaRepository.existsById(cuentaId)) {
            throw new CuentaNoEncontradaException(cuentaId);
        }
        return movimientoRepository.findByCuentaIdAndEstadoOrderByFechaDescIdDesc(cuentaId, EstadoMovimiento.APLICADO).stream()
                .map(this::aDTO)
                .toList();
    }

    private CuentaDTO aDTO(CuentaEntity entity) {
        return new CuentaDTO(
                entity.getCuentaId(), entity.getNombre(), entity.getSaldo(),
                entity.getEdad(), entity.getTipo());
    }

    private MovimientoDTO aDTO(MovimientoEntity entity) {
        return new MovimientoDTO(
                entity.getCuentaId(), entity.getFecha(), entity.getTipoMovimiento(),
                entity.getMonto(), entity.getDescripcion());
    }
}
