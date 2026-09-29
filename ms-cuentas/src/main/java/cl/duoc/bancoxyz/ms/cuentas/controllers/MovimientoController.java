package cl.duoc.bancoxyz.ms.cuentas.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.ms.cuentas.dtos.SolicitudMovimientoDTO;
import cl.duoc.bancoxyz.ms.cuentas.dtos.SolicitudMovimientoRequest;
import cl.duoc.bancoxyz.ms.cuentas.services.MovimientoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/core/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping
    public ResponseEntity<SolicitudMovimientoDTO> solicitar(@RequestBody SolicitudMovimientoRequest solicitud)
            throws Exception {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(movimientoService.solicitar(solicitud));
    }

    @GetMapping("/solicitudes/{solicitudId}")
    public SolicitudMovimientoDTO obtenerSolicitud(@PathVariable String solicitudId) {
        return movimientoService.obtenerSolicitud(solicitudId);
    }
}
