package cl.duoc.bancoxyz.bff.web.dtos;

import org.springframework.http.HttpStatus;

public record RespuestaSolicitud(
        HttpStatus estadoHttp,
        SolicitudMovimientoWebDTO cuerpo) {
}
