package cl.duoc.bancoxyz.ms.validaciones.services;

public record ResultadoValidacion(
        boolean aprobado,
        String motivo) {

    public static ResultadoValidacion aprobar() {
        return new ResultadoValidacion(true, null);
    }

    public static ResultadoValidacion rechazar(String codigo, String detalle) {
        return new ResultadoValidacion(false, codigo + ": " + detalle);
    }
}
