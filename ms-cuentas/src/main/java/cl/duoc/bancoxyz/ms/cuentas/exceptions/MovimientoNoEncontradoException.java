package cl.duoc.bancoxyz.ms.cuentas.exceptions;

public class MovimientoNoEncontradoException extends RuntimeException {

    public MovimientoNoEncontradoException(Long movimientoId) {
        super("No existe el movimiento " + movimientoId + " referido por el evento");
    }
}
