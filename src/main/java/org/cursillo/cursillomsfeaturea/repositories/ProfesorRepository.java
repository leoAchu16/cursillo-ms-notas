package org.cursillo.cursillomsfeaturea.repositories;

import org.cursillo.commons.entities.Usuarios.Profesor;
// NOTA: Ajusta el paquete según la ubicación real de tu entidad Profesor
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Integer> {
    // Al extender JpaRepository<Profesor, Integer>, hereda automáticamente:
    // - getReferenceById(Integer id) -> Crea la referencia Proxy para la clave foránea
    // - findById(Integer id)
    // - save, existsById, deleteById, etc.
}