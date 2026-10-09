package org.cursillo.cursillomsfeaturea.repositories;

import org.cursillo.commons.entities.Examenes.Examen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamenRepository extends JpaRepository<Examen, Integer> {

    // Listar todos los que no tienen borrado lógico
    Page<Examen> findByActivoTrue(Pageable pageable);

    // Filtro requerido: Búsqueda por título del examen (ej. "Parcial 1")
    Page<Examen> findByTituloContainingIgnoreCaseAndActivoTrue(String titulo, Pageable pageable);
}