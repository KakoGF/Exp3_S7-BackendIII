package cl.duoc.bancoxyz.ms.notificaciones.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.bancoxyz.ms.notificaciones.clients.ProveedorNotificacionesSimulado;
import cl.duoc.bancoxyz.ms.notificaciones.models.Notificacion;
import cl.duoc.bancoxyz.ms.notificaciones.services.NotificacionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final ProveedorNotificacionesSimulado proveedor;

    @GetMapping
    public List<Notificacion> listar() {
        return notificacionService.listarNotificaciones();
    }

    @GetMapping("/proveedor")
    public Map<String, Object> estadoProveedor() {
        return Map.of("proveedorFallando", proveedor.estaFallando());
    }

    @PostMapping("/proveedor/falla")
    public Map<String, Object> simularFalla(@RequestParam boolean activa) {
        proveedor.simularFalla(activa);
        return Map.of("proveedorFallando", proveedor.estaFallando());
    }
}
