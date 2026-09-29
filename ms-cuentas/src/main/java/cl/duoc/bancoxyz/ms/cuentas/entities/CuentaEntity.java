package cl.duoc.bancoxyz.ms.cuentas.entities;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cuenta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaEntity {

    @Id
    private Long cuentaId;

    private String nombre;
    private BigDecimal saldo;
    private Integer edad;
    private String tipo;
}
