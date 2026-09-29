package cl.duoc.bancoxyz.ms.validaciones.exceptions;

public class EventoInvalidoException extends RuntimeException {

    public EventoInvalidoException(String mensaje) {
        super(mensaje);
    }

    public EventoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
