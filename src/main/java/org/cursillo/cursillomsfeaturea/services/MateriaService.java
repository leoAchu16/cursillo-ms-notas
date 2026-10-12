package org.cursillo.cursillomsfeaturea.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.MateriaDTO;
import org.cursillo.commons.entities.Materias.Materia;
import org.cursillo.cursillomsfeaturea.mapper.MateriaMapper;
import org.cursillo.cursillomsfeaturea.repositories.MateriaRepository;
import org.cursillo.cursillomsfeaturea.repositories.ProfesorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final ProfesorRepository profesorRepository; // Repositorio para obtener la referencia
    private final MateriaMapper materiaMapper; // Inyectamos el mapper generado

    @Transactional
    public MateriaDTO crear(MateriaDTO dto) {
        log.info("Iniciando creación de materia con nombre: {}", dto.getNombreMateria());


        Materia materia = materiaMapper.toEntity(dto);

        // 2. Asignar la referencia real de JPA para el Profesor (sin hacer SELECT extra)
        materia.setProfesor(profesorRepository.getReferenceById(dto.getIdProfesor()));
        materia.setActivo(true); // Toda materia nueva nace activa

        materia = materiaRepository.save(materia);
        log.info("Materia creada exitosamente con ID: {}", materia.getIdMateria());

        return materiaMapper.toDto(materia);
    }

    @Transactional
    public MateriaDTO actualizar(Integer id, MateriaDTO dto) {
        log.info("Actualizando materia con ID: {}", id);

        // Usamos la consulta optimizada de SQL (findByIdAndActivoTrue)
        Materia materia = materiaRepository.findByIdMateriaAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Materia no encontrada con ID: " + id));

        // Magia de MapStruct: Actualiza la entidad automáticamente (incluyendo los Enums como DiaSemana)
        materiaMapper.updateEntityFromDto(dto, materia);

        // Si el DTO trae un cambio de profesor, actualizamos la relación con un Proxy
        if (dto.getIdProfesor() != null && !dto.getIdProfesor().equals(materia.getProfesor().getIdUsuario())) {
            materia.setProfesor(profesorRepository.getReferenceById(dto.getIdProfesor()));
        }

        return materiaMapper.toDto(materiaRepository.save(materia));
    }

    @Transactional(readOnly = true)
    public MateriaDTO buscarPorId(Integer id) {
        log.info("Buscando materia por ID: {}", id);
        // Filtrado a nivel de base de datos SQL
        return materiaRepository.findByIdMateriaAndActivoTrue(id)
                .map(materiaMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Materia no encontrada o inactiva"));
    }

    @Transactional(readOnly = true)
    public Page<MateriaDTO> listarTodos(Pageable pageable) {
        log.info("Listando materias con paginación");
        return materiaRepository.findByActivoTrue(pageable).map(materiaMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<MateriaDTO> buscarPorNombre(String nombre, Pageable pageable) {
        log.info("Buscando materias por nombre: {}", nombre);
        return materiaRepository.findByNombreMateriaContainingIgnoreCaseAndActivoTrue(nombre, pageable).map(materiaMapper::toDto);
    }

    @Transactional
    public void eliminarLogico(Integer id) {
        log.info("Ejecutando borrado lógico para la materia ID: {}", id);
        Materia materia = materiaRepository.findByIdMateriaAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Materia no encontrada con ID: " + id));

        materia.setActivo(false);
        // Aprovechamos el "Dirty Checking" de Hibernate:
        // No hace falta materiaRepository.save(materia);
        log.info("Materia ID: {} marcada como inactiva", id);
    }

    @Transactional(readOnly = true)
    public Page<MateriaDTO> buscarPorProfesor(Integer idProfesor, Pageable pageable) {
        log.info("Buscando materias activas para el profesor ID: {}", idProfesor);
        return materiaRepository.findByProfesor_IdUsuarioAndActivoTrue(idProfesor, pageable)
                .map(materiaMapper::toDto);
    }
}