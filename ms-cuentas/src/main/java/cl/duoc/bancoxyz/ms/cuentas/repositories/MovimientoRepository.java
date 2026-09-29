package cl.duoc.bancoxyz.ms.cuentas.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.bancoxyz.ms.cuentas.entities.EstadoMovimiento;
import cl.duoc.bancoxyz.ms.cuentas.entities.MovimientoEntity;

public interface MovimientoRepository extends JpaRepository<MovimientoEntity, Long> {

    List<MovimientoEntity> findByCuentaIdAndEstadoOrderByFechaDescIdDesc(Long cuentaId, EstadoMovimiento estado);

    Optional<MovimientoEntity> findBySolicitudId(String solicitudId);

    List<MovimientoEntity> findByCuentaIdAndSolicitudIdIsNotNullOrderByIdDesc(Long cuentaId);
}
