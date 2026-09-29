package cl.duoc.bancoxyz.bff.web.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.bff.web.clients.MsCuentasClient;
import cl.duoc.bancoxyz.bff.web.dtos.CuentaWebDetalleDTO;
import cl.duoc.bancoxyz.bff.web.dtos.CuentaWebResumenDTO;
import cl.duoc.bancoxyz.bff.web.dtos.MovimientoWebDTO;
import cl.duoc.bancoxyz.bff.web.dtos.ResumenMovimientosDTO;
import cl.duoc.bancoxyz.bff.web.dtos.core.CuentaCoreDTO;
import cl.duoc.bancoxyz.bff.web.dtos.core.MovimientoCoreDTO;
import cl.duoc.bancoxyz.bff.web.exceptions.CuentaNoEncontradaException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BffWebService {

    private static final String TEXTO_NO_DISPONIBLE = "Informacion no disponible temporalmente";
    private static final ResumenMovimientosDTO RESUMEN_NO_DISPONIBLE = new ResumenMovimientosDTO(
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

    private final MsCuentasClient msCuentasClient;
    private final Executor bffWebTaskExecutor;

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "listarCuentasFallback")
    public List<CuentaWebResumenDTO> listarCuentas() {
        return msCuentasClient.obtenerCuentas().stream()
                .map(this::aResumen)
                .toList();
    }

    @CircuitBreaker(name = "msCuentas", fallbackMethod = "obtenerDetalleFallback")
    public CuentaWebDetalleDTO obtenerDetalle(Long cuentaId) {
        CompletableFuture<CuentaCoreDTO> cuentaFuture = CompletableFuture.supplyAsync(
                () -> msCuentasClient.obtenerCuenta(cuentaId), bffWebTaskExecutor);
        CompletableFuture<List<MovimientoCoreDTO>> movimientosFuture = CompletableFuture.supplyAsync(
                () -> msCuentasClient.obtenerMovimientos(cuentaId), bffWebTaskExecutor);

        CuentaCoreDTO cuenta = esperar(cuentaFuture);
        List<MovimientoCoreDTO> movimientos = esperar(movimientosFuture);

        List<MovimientoWebDTO> movimientosWeb = movimientos.stream()
                .map(m -> new MovimientoWebDTO(m.fecha(), m.tipoMovimiento(), m.monto(), m.descripcion()))
                .toList();

        return new CuentaWebDetalleDTO(
                cuenta.cuentaId(), cuenta.nombre(), cuenta.saldo(), cuenta.tipo(), cuenta.edad(),
                movimientosWeb, calcularResumen(movimientos));
    }

    @SuppressWarnings("unused")
    private List<CuentaWebResumenDTO> listarCuentasFallback(Throwable error) {
        propagarSiEsNoEncontrada(error);
        log.warn("Fallback activado al listar cuentas: se responde listado vacio. Causa: {}", error.toString());
        return List.of();
    }

    @SuppressWarnings("unused")
    private CuentaWebDetalleDTO obtenerDetalleFallback(Long cuentaId, Throwable error) {
        propagarSiEsNoEncontrada(error);
        log.warn("Fallback activado en el detalle de la cuenta {}: se responde version degradada. Causa: {}",
                cuentaId, error.toString());
        return new CuentaWebDetalleDTO(
                cuentaId, TEXTO_NO_DISPONIBLE, null, TEXTO_NO_DISPONIBLE, null,
                List.of(), RESUMEN_NO_DISPONIBLE);
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

    private CuentaWebResumenDTO aResumen(CuentaCoreDTO cuenta) {
        return new CuentaWebResumenDTO(
                cuenta.cuentaId(), cuenta.nombre(), cuenta.saldo(), cuenta.tipo(), cuenta.edad());
    }

    private ResumenMovimientosDTO calcularResumen(List<MovimientoCoreDTO> movimientos) {
        BigDecimal totalDepositos = sumarPorTipo(movimientos, "DEPOSITO");
        BigDecimal totalRetiros = sumarPorTipo(movimientos, "RETIRO");
        BigDecimal totalCompras = sumarPorTipo(movimientos, "COMPRA");
        BigDecimal totalPagos = sumarPorTipo(movimientos, "PAGO");
        BigDecimal saldoNeto = movimientos.stream()
                .map(MovimientoCoreDTO::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumenMovimientosDTO(
                totalDepositos, totalRetiros, totalCompras, totalPagos, saldoNeto, movimientos.size());
    }

    private BigDecimal sumarPorTipo(List<MovimientoCoreDTO> movimientos, String tipo) {
        return movimientos.stream()
                .filter(m -> tipo.equals(m.tipoMovimiento()))
                .map(MovimientoCoreDTO::monto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
