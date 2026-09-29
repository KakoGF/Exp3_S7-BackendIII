package cl.duoc.bancoxyz.bff.cajero.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.bff.cajero.dtos.ComprobanteCajeroDTO;
import cl.duoc.bancoxyz.bff.cajero.dtos.RespuestaSolicitud;
import cl.duoc.bancoxyz.bff.cajero.dtos.SolicitudMovimientoRequest;
import cl.duoc.bancoxyz.bff.cajero.services.SolicitudMovimientoCajeroService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bff/cajero")
@RequiredArgsConstructor
public class MovimientoCajeroController {

    private final SolicitudMovimientoCajeroService solicitudMovimientoService;

    @PostMapping("/cuentas/{cuentaId}/movimientos")
    public ResponseEntity<ComprobanteCajeroDTO> solicitar(
            @PathVariable Long cuentaId,
            @RequestBody SolicitudMovimientoRequest solicitud) {
        RespuestaSolicitud respuesta = solicitudMovimientoService.solicitar(cuentaId, solicitud);
        return ResponseEntity.status(respuesta.estadoHttp()).body(respuesta.cuerpo());
    }

    @GetMapping("/movimientos/solicitudes/{solicitudId}")
    public ComprobanteCajeroDTO consultar(@PathVariable String solicitudId) {
        return solicitudMovimientoService.consultar(solicitudId);
    }
}
