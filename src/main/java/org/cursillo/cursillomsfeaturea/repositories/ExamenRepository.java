package org.cursillo.cursillomsfeaturea.repositories;

import org.cursillo.commons.entities.Examenes.Examen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Integer> {

    // Cambiado: idExamen en lugar de id
    Optional<Examen> findByIdExamenAndActivoTrue(Integer idExamen);

    Page<Examen> findByActivoTrue(Pageable pageable);
    Page<Examen> findByMateria_IdMateriaAndActivoTrue(Integer idMateria, Pageable pageable);

    Page<Examen> findByTituloContainingIgnoreCaseAndActivoTrue(String titulo, Pageable pageable);
}