package cl.duoc.bancoxyz.ms.cuentas.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.ms.cuentas.dtos.CuentaDTO;
import cl.duoc.bancoxyz.ms.cuentas.dtos.MovimientoDTO;
import cl.duoc.bancoxyz.ms.cuentas.dtos.SolicitudMovimientoDTO;
import cl.duoc.bancoxyz.ms.cuentas.services.CuentaService;
import cl.duoc.bancoxyz.ms.cuentas.services.MovimientoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/core/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;
    private final MovimientoService movimientoService;

    @GetMapping
    public List<CuentaDTO> listarCuentas() {
        return cuentaService.listarCuentas();
    }

    @GetMapping("/{cuentaId}")
    public CuentaDTO obtenerCuenta(@PathVariable Long cuentaId) {
        return cuentaService.obtenerCuenta(cuentaId);
    }

    @GetMapping("/{cuentaId}/movimientos")
    public List<MovimientoDTO> listarMovimientos(@PathVariable Long cuentaId) {
        return cuentaService.listarMovimientos(cuentaId);
    }

    @GetMapping("/{cuentaId}/solicitudes")
    public List<SolicitudMovimientoDTO> listarSolicitudes(@PathVariable Long cuentaId) {
        return movimientoService.listarSolicitudes(cuentaId);
    }
}
