package cl.duoc.bancoxyz.ms.cuentas.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evento_procesado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventoProcesadoEntity {

    @Id
    @Column(length = 64)
    private String eventId;

    @Column(length = 60)
    private String tipoEvento;

    private LocalDateTime fechaProceso;
}
