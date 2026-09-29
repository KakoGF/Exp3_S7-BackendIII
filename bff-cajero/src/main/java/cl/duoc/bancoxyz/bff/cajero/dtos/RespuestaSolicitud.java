package cl.duoc.bancoxyz.bff.cajero.dtos;

import org.springframework.http.HttpStatus;

public record RespuestaSolicitud(
        HttpStatus estadoHttp,
        ComprobanteCajeroDTO cuerpo) {
}
