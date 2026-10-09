package org.cursillo.cursillomsfeaturea.repositories;

import org.cursillo.commons.entities.Notas.Nota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Integer> {

    // Obtener las notas que no están borradas lógicamente
    Page<Nota> findByActivoTrue(Pageable pageable);

    // Búsqueda con filtro: Obtener todas las notas de un examen específico
    Page<Nota> findByExamen_IdExamenAndActivoTrue(Integer idExamen, Pageable pageable);
}