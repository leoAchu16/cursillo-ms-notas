package org.cursillo.cursillomsfeaturea.repositories;
import org.cursillo.commons.entities.Materias.Materia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Integer> {
    Page<Materia> findByActivoTrue(Pageable pageable);
    Page<Materia> findByNombreMateriaContainingIgnoreCaseAndActivoTrue(String nombreMateria, Pageable pageable);
}