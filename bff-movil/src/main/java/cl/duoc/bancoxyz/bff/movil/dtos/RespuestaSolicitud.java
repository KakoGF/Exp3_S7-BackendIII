package cl.duoc.bancoxyz.bff.movil.dtos;

import org.springframework.http.HttpStatus;

public record RespuestaSolicitud(
        HttpStatus estadoHttp,
        SolicitudMovimientoMovilDTO cuerpo) {
}
