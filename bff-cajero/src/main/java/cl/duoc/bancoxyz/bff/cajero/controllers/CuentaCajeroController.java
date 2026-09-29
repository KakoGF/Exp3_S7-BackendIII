package cl.duoc.bancoxyz.bff.cajero.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.bff.cajero.dtos.CuentaCajeroSaldoDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.MovimientoCajeroDTO;
import cl.duoc.bancoxyz.bff.cajero.services.BffCajeroService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bff/cajero/cuentas")
@RequiredArgsConstructor
public class CuentaCajeroController {

    private final BffCajeroService bffCajeroService;

    @GetMapping("/{cuentaId}/saldo")
    public CuentaCajeroSaldoDTO consultarSaldo(@PathVariable Long cuentaId) {
        return bffCajeroService.consultarSaldo(cuentaId);
    }

    @GetMapping("/{cuentaId}/movimientos/ultimo")
    public ResponseEntity<MovimientoCajeroDTO> consultarUltimoMovimiento(@PathVariable Long cuentaId) {
        return bffCajeroService.consultarUltimoMovimiento(cuentaId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
