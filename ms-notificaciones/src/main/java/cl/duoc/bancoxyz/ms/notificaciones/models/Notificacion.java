package cl.duoc.bancoxyz.ms.notificaciones.models;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Notificacion {

    private String notificacionId;
    private String eventId;
    private String solicitudId;
    private Long cuentaId;
    private String nombreCliente;
    private String canal;
    private String mensaje;
    private EstadoNotificacion estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
}
