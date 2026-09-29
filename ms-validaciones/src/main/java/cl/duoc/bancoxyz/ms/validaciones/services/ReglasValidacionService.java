package cl.duoc.bancoxyz.ms.validaciones.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cl.duoc.bancoxyz.ms.validaciones.events.MovimientoSolicitadoEvent;

@Service
public class ReglasValidacionService {

    private static final String RETIRO = "RETIRO";
    private static final String CAJERO = "CAJERO";

    private final Map<String, BigDecimal> limitePorCanal;
    private final BigDecimal multiploRetiroCajero;
    private final Set<String> tiposSinRetiro;
    private final int maxSolicitudesVentana;
    private final long ventanaSegundos;
    private final Map<Long, Deque<Instant>> solicitudesPorCuenta = new HashMap<>();

    public ReglasValidacionService(
            @Value("${validaciones.limite.web}") BigDecimal limiteWeb,
            @Value("${validaciones.limite.movil}") BigDecimal limiteMovil,
            @Value("${validaciones.limite.cajero}") BigDecimal limiteCajero,
            @Value("${validaciones.cajero.multiplo-retiro}") BigDecimal multiploRetiroCajero,
            @Value("${validaciones.tipos-cuenta-sin-retiro}") String tiposSinRetiro,
            @Value("${validaciones.antifraude.max-solicitudes}") int maxSolicitudesVentana,
            @Value("${validaciones.antifraude.ventana-segundos}") long ventanaSegundos) {
        this.limitePorCanal = Map.of("WEB", limiteWeb, "MOVIL", limiteMovil, CAJERO, limiteCajero);
        this.multiploRetiroCajero = multiploRetiroCajero;
        this.tiposSinRetiro = Arrays.stream(tiposSinRetiro.split(","))
                .map(t -> t.trim().toUpperCase(Locale.ROOT))
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        this.maxSolicitudesVentana = maxSolicitudesVentana;
        this.ventanaSegundos = ventanaSegundos;
    }

    public ResultadoValidacion evaluar(MovimientoSolicitadoEvent evento) {
        String canal = evento.canal().toUpperCase(Locale.ROOT);
        String tipoMovimiento = evento.tipoMovimiento().toUpperCase(Locale.ROOT);
        String tipoCuenta = evento.tipoCuenta() == null ? "" : evento.tipoCuenta().toUpperCase(Locale.ROOT);

        BigDecimal limite = limitePorCanal.get(canal);
        if (limite != null && evento.monto().compareTo(limite) > 0) {
            return ResultadoValidacion.rechazar("MONTO_EXCEDE_LIMITE_CANAL",
                    "el canal " + canal + " permite operaciones de hasta " + limite.toPlainString());
        }

        if (RETIRO.equals(tipoMovimiento) && tiposSinRetiro.contains(tipoCuenta)) {
            return ResultadoValidacion.rechazar("TIPO_CUENTA_NO_PERMITE_RETIRO",
                    "las cuentas de tipo " + tipoCuenta + " no permiten retiros");
        }

        if (CAJERO.equals(canal) && RETIRO.equals(tipoMovimiento)
                && evento.monto().remainder(multiploRetiroCajero).compareTo(BigDecimal.ZERO) != 0) {
            return ResultadoValidacion.rechazar("MONTO_NO_MULTIPLO_DE_BILLETES",
                    "el cajero solo entrega multiplos de " + multiploRetiroCajero.toPlainString());
        }

        int solicitudesRecientes = registrarYContar(evento.cuentaId());
        if (solicitudesRecientes > maxSolicitudesVentana) {
            return ResultadoValidacion.rechazar("ACTIVIDAD_SOSPECHOSA",
                    solicitudesRecientes + " solicitudes en " + ventanaSegundos + " segundos para la misma cuenta");
        }

        return ResultadoValidacion.aprobar();
    }

    private synchronized int registrarYContar(Long cuentaId) {
        Instant ahora = Instant.now();
        Instant limiteVentana = ahora.minusSeconds(ventanaSegundos);
        Deque<Instant> historial = solicitudesPorCuenta.computeIfAbsent(cuentaId, id -> new ArrayDeque<>());
        while (!historial.isEmpty() && historial.peekFirst().isBefore(limiteVentana)) {
            historial.pollFirst();
        }
        historial.addLast(ahora);
        return historial.size();
    }
}
