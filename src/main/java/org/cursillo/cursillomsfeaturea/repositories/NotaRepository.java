package org.cursillo.cursillomsfeaturea.repositories;

import org.cursillo.commons.entities.Notas.Nota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Integer> {
    Optional<Nota> findByIdNotaAndActivoTrue(Integer idNota);//obligo al servisio verificar si la nota existe antes de usarla

    Page<Nota> findByActivoTrue(Pageable pageable);//limintes de paginacion
    Page<Nota> findByExamen_IdExamenAndActivoTrue(Integer idExamen, Pageable pageable);//buesqueda de id exmen espesifico por id
    Page<Nota> findByExamen_IdExamenAndAlumno_NombreContainingIgnoreCaseAndActivoTrue(
            Integer idExamen, String nombre, Pageable pageable);
}