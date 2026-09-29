package cl.duoc.bancoxyz.bff.movil.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.bff.movil.dtos.SolicitudMovimientoMovilDTO;
import cl.duoc.bancoxyz.bff.movil.dtos.RespuestaSolicitud;
import cl.duoc.bancoxyz.bff.movil.dtos.SolicitudMovimientoRequest;
import cl.duoc.bancoxyz.bff.movil.services.SolicitudMovimientoMovilService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bff/movil")
@RequiredArgsConstructor
public class MovimientoMovilController {

    private final SolicitudMovimientoMovilService solicitudMovimientoService;

    @PostMapping("/cuentas/{cuentaId}/movimientos")
    public ResponseEntity<SolicitudMovimientoMovilDTO> solicitar(
            @PathVariable Long cuentaId,
            @RequestBody SolicitudMovimientoRequest solicitud) {
        RespuestaSolicitud respuesta = solicitudMovimientoService.solicitar(cuentaId, solicitud);
        return ResponseEntity.status(respuesta.estadoHttp()).body(respuesta.cuerpo());
    }

    @GetMapping("/movimientos/solicitudes/{solicitudId}")
    public SolicitudMovimientoMovilDTO consultar(@PathVariable String solicitudId) {
        return solicitudMovimientoService.consultar(solicitudId);
    }
}
