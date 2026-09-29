package cl.duoc.bancoxyz.ms.cuentas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.bancoxyz.ms.cuentas.entities.EventoProcesadoEntity;

public interface EventoProcesadoRepository extends JpaRepository<EventoProcesadoEntity, String> {
}
