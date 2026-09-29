package cl.duoc.bancoxyz.ms.cuentas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.bancoxyz.ms.cuentas.entities.CuentaEntity;

public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {
}
