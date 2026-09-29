package cl.duoc.bancoxyz.bff.web.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.bff.web.dtos.CuentaWebDetalleDTO;
import cl.duoc.bancoxyz.bff.web.dtos.CuentaWebResumenDTO;
import cl.duoc.bancoxyz.bff.web.services.BffWebService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bff/web/cuentas")
@RequiredArgsConstructor
public class CuentaWebController {

    private final BffWebService bffWebService;

    @GetMapping
    public List<CuentaWebResumenDTO> listarCuentas() {
        return bffWebService.listarCuentas();
    }

    @GetMapping("/{cuentaId}")
    public CuentaWebDetalleDTO obtenerDetalle(@PathVariable Long cuentaId) {
        return bffWebService.obtenerDetalle(cuentaId);
    }
}
