package cl.duoc.bancoxyz.ms.cuentas.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cuentaId;
    private LocalDate fecha;
    private String tipoMovimiento;
    private BigDecimal monto;
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoMovimiento estado;

    @Column(length = 20)
    private String canal;

    @Column(unique = true, length = 64)
    private String solicitudId;

    private String motivo;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaResolucion;
}
