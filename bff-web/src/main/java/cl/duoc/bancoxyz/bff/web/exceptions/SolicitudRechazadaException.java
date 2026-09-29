package cl.duoc.bancoxyz.bff.web.exceptions;

public class SolicitudRechazadaException extends RuntimeException {

    private final int estadoHttp;

    public SolicitudRechazadaException(int estadoHttp, String mensaje) {
        super(mensaje);
        this.estadoHttp = estadoHttp;
    }

    public int getEstadoHttp() {
        return estadoHttp;
    }
}
