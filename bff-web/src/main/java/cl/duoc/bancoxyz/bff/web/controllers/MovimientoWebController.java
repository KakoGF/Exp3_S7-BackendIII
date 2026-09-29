package cl.duoc.bancoxyz.bff.web.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.bff.web.dtos.SolicitudMovimientoWebDTO;
import cl.duoc.bancoxyz.bff.web.dtos.RespuestaSolicitud;
import cl.duoc.bancoxyz.bff.web.dtos.SolicitudMovimientoRequest;
import cl.duoc.bancoxyz.bff.web.services.SolicitudMovimientoWebService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bff/web")
@RequiredArgsConstructor
public class MovimientoWebController {

    private final SolicitudMovimientoWebService solicitudMovimientoService;

    @PostMapping("/cuentas/{cuentaId}/movimientos")
    public ResponseEntity<SolicitudMovimientoWebDTO> solicitar(
            @PathVariable Long cuentaId,
            @RequestBody SolicitudMovimientoRequest solicitud) {
        RespuestaSolicitud respuesta = solicitudMovimientoService.solicitar(cuentaId, solicitud);
        return ResponseEntity.status(respuesta.estadoHttp()).body(respuesta.cuerpo());
    }

    @GetMapping("/movimientos/solicitudes/{solicitudId}")
    public SolicitudMovimientoWebDTO consultar(@PathVariable String solicitudId) {
        return solicitudMovimientoService.consultar(solicitudId);
    }
}
