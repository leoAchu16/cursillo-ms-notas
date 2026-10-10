package org.cursillo.cursillomsfeaturea.repositories;
import org.cursillo.commons.entities.Materias.Materia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Integer> {

    Optional<Materia> findByIdMateriaAndActivoTrue(Integer idMateria);

    Page<Materia> findByActivoTrue(Pageable pageable);
    Page<Materia> findByProfesor_IdUsuarioAndActivoTrue(Integer idProfesor, Pageable pageable);

    Page<Materia> findByNombreMateriaContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);
}