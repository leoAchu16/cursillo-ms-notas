package org.cursillo.cursillomsfeaturea.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.ExamenDTO;
import org.cursillo.commons.entities.Examenes.Examen;
import org.cursillo.cursillomsfeaturea.mapper.ExamenMapper;
import org.cursillo.cursillomsfeaturea.repositories.ExamenRepository;
import org.cursillo.cursillomsfeaturea.repositories.MateriaRepository;
import org.cursillo.cursillomsfeaturea.repositories.ProfesorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamenService {

    private final ExamenRepository examenRepository;
    private final MateriaRepository materiaRepository;
    private final ProfesorRepository profesorRepository;
    private final ExamenMapper examenMapper;

    @Transactional
    public ExamenDTO crear(ExamenDTO dto) {
        log.info("Iniciando creación de examen: {}", dto.getTitulo());

        // 1. Mapeo básico de atributos primitivos y enums mediante MapStruct
        Examen examen = examenMapper.toEntity(dto);

        // 2. Asignación optimizada de FKs usando Proxies de JPA (evita SELECTs extra)
        if (dto.getIdMateria() != null) {
            examen.setMateria(materiaRepository.getReferenceById(dto.getIdMateria()));
        }
        if (dto.getIdProfesor() != null) {
            examen.setProfesor(profesorRepository.getReferenceById(dto.getIdProfesor()));
        }

        examen.setActivo(true); // El nuevo examen nace activo

        examen = examenRepository.save(examen);
        log.info("Examen creado exitosamente con ID: {}", examen.getIdExamen());

        return examenMapper.toDto(examen);
    }

    @Transactional
    public ExamenDTO actualizar(Integer id, ExamenDTO dto) {
        log.info("Actualizando examen ID: {}", id);

        // Búsqueda ajustada a idExamen
        Examen examen = examenRepository.findByIdExamenAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Examen no encontrado con ID: " + id));

        // Actualización de atributos básicos con MapStruct
        examenMapper.updateEntityFromDto(dto, examen);

        // Actualización limpia de relaciones FK mediante proxies
        if (dto.getIdMateria() != null && (examen.getMateria() == null || !dto.getIdMateria().equals(examen.getMateria().getIdMateria()))) {
            examen.setMateria(materiaRepository.getReferenceById(dto.getIdMateria()));
        }

        if (dto.getIdProfesor() != null && (examen.getProfesor() == null || !dto.getIdProfesor().equals(examen.getProfesor().getIdUsuario()))) {
            examen.setProfesor(profesorRepository.getReferenceById(dto.getIdProfesor()));
        }

        return examenMapper.toDto(examenRepository.save(examen));
    }

    @Transactional(readOnly = true)
    public ExamenDTO buscarPorId(Integer id) {
        log.info("Buscando examen ID: {}", id);
        return examenRepository.findByIdExamenAndActivoTrue(id)
                .map(examenMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Examen no encontrado o inactivo con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ExamenDTO> listarTodos(Pageable pageable) {
        log.info("Listando exámenes activos (Paginado)");
        return examenRepository.findByActivoTrue(pageable).map(examenMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ExamenDTO> buscarPorTitulo(String titulo, Pageable pageable) {
        log.info("Buscando exámenes por título: {}", titulo);
        return examenRepository.findByTituloContainingIgnoreCaseAndActivoTrue(titulo, pageable)
                .map(examenMapper::toDto);
    }

    @Transactional
    public void eliminarLogico(Integer id) {
        log.info("Ejecutando borrado lógico para el examen ID: {}", id);

        Examen examen = examenRepository.findByIdExamenAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Examen no encontrado con ID: " + id));

        examen.setActivo(false);
        // Dirty checking de Hibernate realiza el UPDATE de forma automática al cerrar la transacción
        log.info("Examen ID: {} marcado como inactivo", id);
    }
    @Transactional(readOnly = true)
    public Page<ExamenDTO> buscarPorMateria(Integer idMateria, Pageable pageable) {
        log.info("Buscando exámenes activos para la materia ID: {}", idMateria);
        return examenRepository.findByMateria_IdMateriaAndActivoTrue(idMateria, pageable)
                .map(examenMapper::toDto);
    }
}