package org.cursillo.cursillomsfeaturea.repositories;

import org.cursillo.commons.entities.Usuarios.Alumno;
// Si tu clase Alumno está bajo otra ruta dentro de commons, ajusta el import de la entidad
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {
    // Hereda getReferenceById(Integer), findById, save, etc.
}
