package cl.duoc.bancoxyz.bff.cajero.services;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.bff.cajero.clients.MsCuentasClient;
import cl.duoc.bancoxyz.bff.cajero.dtos.CuentaCajeroSaldoDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.MovimientoCajeroDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.core.CuentaCoreDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.core.MovimientoCoreDTO;
import cl.duoc.bancoxyz.bff.cajero.exceptions.CuentaNoEncontradaException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BffCajeroService {

    private static final String TIPO_NO_DISPONIBLE = "NO_DISPONIBLE";

    private final MsCuentasClient msCuentasClient;

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "consultarSaldoFallback")
    public CuentaCajeroSaldoDTO consultarSaldo(Long cuentaId) {
        CuentaCoreDTO cuenta = msCuentasClient.obtenerCuenta(cuentaId);
        return new CuentaCajeroSaldoDTO(cuenta.cuentaId(), cuenta.saldo(), cuenta.tipo());
    }

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "consultarUltimoMovimientoFallback")
    public Optional<MovimientoCajeroDTO> consultarUltimoMovimiento(Long cuentaId) {
        List<MovimientoCoreDTO> movimientos = msCuentasClient.obtenerMovimientos(cuentaId);
        return movimientos.stream()
                .max(Comparator.comparing(MovimientoCoreDTO::fecha))
                .map(m -> new MovimientoCajeroDTO(m.fecha(), m.tipoMovimiento(), m.monto()));
    }

    @SuppressWarnings("unused")
    private CuentaCajeroSaldoDTO consultarSaldoFallback(Long cuentaId, Throwable error) {
        propagarSiEsNoEncontrada(error);
        log.warn("Fallback activado en el saldo de la cuenta {}: se responde saldo no disponible. Causa: {}",
                cuentaId, error.toString());
        return new CuentaCajeroSaldoDTO(cuentaId, null, TIPO_NO_DISPONIBLE);
    }

    @SuppressWarnings("unused")
    private Optional<MovimientoCajeroDTO> consultarUltimoMovimientoFallback(Long cuentaId, Throwable error) {
        propagarSiEsNoEncontrada(error);
        log.warn("Fallback activado en el ultimo movimiento de la cuenta {}: se responde sin contenido. Causa: {}",
                cuentaId, error.toString());
        return Optional.empty();
    }

    private void propagarSiEsNoEncontrada(Throwable error) {
        if (error instanceof CuentaNoEncontradaException noEncontrada) {
            throw noEncontrada;
        }
    }
}
