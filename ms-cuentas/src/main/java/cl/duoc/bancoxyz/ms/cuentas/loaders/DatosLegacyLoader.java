package cl.duoc.bancoxyz.ms.cuentas.loaders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import cl.duoc.bancoxyz.ms.cuentas.entities.CuentaEntity;
import cl.duoc.bancoxyz.ms.cuentas.entities.EstadoMovimiento;
import cl.duoc.bancoxyz.ms.cuentas.entities.MovimientoEntity;
import cl.duoc.bancoxyz.ms.cuentas.repositories.CuentaRepository;
import cl.duoc.bancoxyz.ms.cuentas.repositories.MovimientoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatosLegacyLoader implements CommandLineRunner {

    private static final DateTimeFormatter[] FORMATOS_FECHA = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };

    private static final String CANAL_LEGACY = "LEGACY";

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    private int cuentasDescartadas = 0;
    private int movimientosDescartados = 0;

    @Override
    public void run(String... args) throws Exception {
        if (cuentaRepository.count() > 0) {
            return;
        }

        List<CuentaEntity> cuentas = leerCuentas();
        cuentaRepository.saveAll(cuentas);

        List<MovimientoEntity> movimientos = leerMovimientos();
        movimientoRepository.saveAll(movimientos);

        log.info(">> Datos legacy cargados: {} cuentas ({} descartadas), {} movimientos ({} descartados)",
                cuentas.size(), cuentasDescartadas, movimientos.size(), movimientosDescartados);
    }

    private List<CuentaEntity> leerCuentas() throws Exception {
        List<CuentaEntity> resultado = new ArrayList<>();
        try (BufferedReader reader = abrirLector("data/intereses.csv")) {
            reader.readLine();
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                CuentaEntity cuenta = parsearCuenta(linea);
                if (cuenta != null) {
                    resultado.add(cuenta);
                } else {
                    cuentasDescartadas++;
                }
            }
        }
        return resultado;
    }

    private CuentaEntity parsearCuenta(String linea) {
        String[] campos = linea.split(",", -1);
        if (campos.length < 5) {
            log.warn("Fila de cuenta con columnas insuficientes, se descarta: {}", linea);
            return null;
        }

        Long cuentaId;
        try {
            cuentaId = Long.parseLong(campos[0].trim());
        } catch (NumberFormatException ex) {
            log.warn("Fila de cuenta sin cuenta_id valido, se descarta: {}", linea);
            return null;
        }

        String nombre = campos[1].trim();
        if (nombre.isEmpty()) {
            log.warn("Fila de cuenta {} sin nombre, se descarta", cuentaId);
            return null;
        }

        String saldoTexto = campos[2].trim();
        String edadTexto = campos[3].trim();
        String tipoTexto = campos[4].trim();

        if (saldoTexto.isEmpty()) {
            log.warn("Fila de cuenta {} sin saldo, se descarta (dato invalido forzado)", cuentaId);
            return null;
        }
        if (edadTexto.isEmpty()) {
            log.warn("Fila de cuenta {} sin edad, se descarta (dato invalido forzado)", cuentaId);
            return null;
        }
        if (tipoTexto.isEmpty() || tipoTexto.equals("-1") || tipoTexto.equalsIgnoreCase("unknown")) {
            log.warn("Fila de cuenta {} con tipo invalido ({}), se descarta (dato invalido forzado)",
                    cuentaId, tipoTexto);
            return null;
        }

        BigDecimal saldo;
        Integer edad;
        try {
            saldo = new BigDecimal(saldoTexto);
            edad = Integer.parseInt(edadTexto);
        } catch (NumberFormatException ex) {
            log.warn("Fila de cuenta {} con saldo o edad no numerico, se descarta", cuentaId);
            return null;
        }

        return new CuentaEntity(cuentaId, nombre, saldo, edad, tipoTexto.toUpperCase(Locale.ROOT));
    }

    private List<MovimientoEntity> leerMovimientos() throws Exception {
        List<MovimientoEntity> resultado = new ArrayList<>();
        try (BufferedReader reader = abrirLector("data/cuentas_anuales.csv")) {
            reader.readLine();
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                MovimientoEntity movimiento = parsearMovimiento(linea);
                if (movimiento != null) {
                    resultado.add(movimiento);
                } else {
                    movimientosDescartados++;
                }
            }
        }
        return resultado;
    }

    private MovimientoEntity parsearMovimiento(String linea) {
        String[] campos = linea.split(",", -1);
        if (campos.length < 5) {
            log.warn("Fila de movimiento con columnas insuficientes, se descarta: {}", linea);
            return null;
        }

        Long cuentaId;
        try {
            cuentaId = Long.parseLong(campos[0].trim());
        } catch (NumberFormatException ex) {
            log.warn("Fila de movimiento sin cuenta_id valido, se descarta: {}", linea);
            return null;
        }

        LocalDate fecha = parsearFecha(campos[1].trim());
        if (fecha == null) {
            log.warn("Fila de movimiento de cuenta {} con fecha no parseable, se descarta: {}", cuentaId, campos[1]);
            return null;
        }

        String tipoMovimiento = normalizarTipoMovimiento(campos[2].trim());

        String montoTexto = campos[3].trim();
        if (montoTexto.isEmpty()) {
            log.warn("Fila de movimiento de cuenta {} sin monto, se descarta", cuentaId);
            return null;
        }
        BigDecimal monto;
        try {
            monto = new BigDecimal(montoTexto);
        } catch (NumberFormatException ex) {
            log.warn("Fila de movimiento de cuenta {} con monto no numerico, se descarta: {}", cuentaId, montoTexto);
            return null;
        }

        String descripcion = campos[4].trim();

        return new MovimientoEntity(null, cuentaId, fecha, tipoMovimiento, monto, descripcion,
                EstadoMovimiento.APLICADO, CANAL_LEGACY, null, null, null, null);
    }

    private LocalDate parsearFecha(String valor) {
        for (DateTimeFormatter formato : FORMATOS_FECHA) {
            try {
                return LocalDate.parse(valor, formato);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private String normalizarTipoMovimiento(String valor) {
        String sinTildes = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toUpperCase(Locale.ROOT);
    }

    private BufferedReader abrirLector(String classpathFile) throws Exception {
        InputStream input = new ClassPathResource(classpathFile).getInputStream();
        return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
    }
}
