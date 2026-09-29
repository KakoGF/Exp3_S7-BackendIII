package cl.duoc.bancoxyz.bff.movil.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.bff.movil.clients.MsCuentasClient;
import cl.duoc.bancoxyz.bff.movil.dtos.CuentaMovilDetalleDTO;
import cl.duoc.bancoxyz.bff.movil.dtos.CuentaMovilResumenDTO;
import cl.duoc.bancoxyz.bff.movil.dtos.MovimientoMovilDTO;
import cl.duoc.bancoxyz.bff.movil.dtos.core.CuentaCoreDTO;
import cl.duoc.bancoxyz.bff.movil.dtos.core.MovimientoCoreDTO;
import cl.duoc.bancoxyz.bff.movil.exceptions.CuentaNoEncontradaException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BffMovilService {

    private static final int MAX_ULTIMOS_MOVIMIENTOS = 3;
    private static final String TEXTO_NO_DISPONIBLE = "Informacion no disponible temporalmente";

    private final MsCuentasClient msCuentasClient;
    private final Executor bffMovilTaskExecutor;

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "listarCuentasFallback")
    public List<CuentaMovilResumenDTO> listarCuentas() {
        return msCuentasClient.obtenerCuentas().stream()
                .map(c -> new CuentaMovilResumenDTO(c.cuentaId(), c.nombre(), c.saldo()))
                .toList();
    }

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "obtenerDetalleFallback")
    public CuentaMovilDetalleDTO obtenerDetalle(Long cuentaId) {
        CompletableFuture<CuentaCoreDTO> cuentaFuture = CompletableFuture.supplyAsync(
                () -> msCuentasClient.obtenerCuenta(cuentaId), bffMovilTaskExecutor);
        CompletableFuture<List<MovimientoCoreDTO>> movimientosFuture = CompletableFuture.supplyAsync(
                () -> msCuentasClient.obtenerMovimientos(cuentaId), bffMovilTaskExecutor);

        CuentaCoreDTO cuenta = esperar(cuentaFuture);
        List<MovimientoCoreDTO> movimientos = esperar(movimientosFuture);

        List<MovimientoMovilDTO> ultimos = movimientos.stream()
                .limit(MAX_ULTIMOS_MOVIMIENTOS)
                .map(m -> new MovimientoMovilDTO(m.fecha().toString(), m.monto(), m.tipoMovimiento()))
                .toList();

        return new CuentaMovilDetalleDTO(cuenta.cuentaId(), cuenta.nombre(), cuenta.saldo(), ultimos);
    }

    @SuppressWarnings("unused")
    private List<CuentaMovilResumenDTO> listarCuentasFallback(Throwable error) {
        propagarSiEsNoEncontrada(error);
        log.warn("Fallback activado al listar cuentas: se responde listado vacio. Causa: {}", error.toString());
        return List.of();
    }

    @SuppressWarnings("unused")
    private CuentaMovilDetalleDTO obtenerDetalleFallback(Long cuentaId, Throwable error) {
        propagarSiEsNoEncontrada(error);
        log.warn("Fallback activado en el detalle de la cuenta {}: se responde version degradada. Causa: {}",
                cuentaId, error.toString());
        return new CuentaMovilDetalleDTO(cuentaId, TEXTO_NO_DISPONIBLE, null, List.of());
    }

    private void propagarSiEsNoEncontrada(Throwable error) {
        Throwable causa = error instanceof CompletionException && error.getCause() != null
                ? error.getCause()
                : error;
        if (causa instanceof CuentaNoEncontradaException noEncontrada) {
            throw noEncontrada;
        }
    }

    private <T> T esperar(CompletableFuture<T> futuro) {
        try {
            return futuro.join();
        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException causa) {
                throw causa;
            }
            throw ex;
        }
    }
}
