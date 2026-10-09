package org.cursillo.cursillomsfeaturea.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.NotaDTO;
import org.cursillo.commons.entities.Notas.Nota;
import org.cursillo.cursillomsfeaturea.mapper.NotaMapper;
import org.cursillo.cursillomsfeaturea.repositories.NotaRepository;
import org.cursillo.cursillomsfeaturea.repositories.ExamenRepository;
import org.cursillo.cursillomsfeaturea.repositories.AlumnoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException; // Usar una excepción específica

@Service
@RequiredArgsConstructor
@Slf4j
public class NotaService {

    private final NotaRepository notaRepository;
    private final ExamenRepository examenRepository;
    private final AlumnoRepository alumnoRepository;
    private final NotaMapper notaMapper;

    @Transactional
    public NotaDTO crear(NotaDTO dto) {
        log.info("Iniciando registro de nota para el alumno ID: {}", dto.getIdAlumno());

        Nota nota = notaMapper.toEntity(dto);
        nota.setExamen(examenRepository.getReferenceById(dto.getIdExamen()));
        nota.setAlumno(alumnoRepository.getReferenceById(dto.getIdAlumno()));
        nota.setActivo(true);

        return notaMapper.toDto(notaRepository.save(nota));
    }

    @Transactional
    public NotaDTO actualizar(Integer id, NotaDTO dto) {
        log.info("Actualizando nota ID: {}", id);

        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada con ID: " + id));

        // Magia de MapStruct: Actualiza el objeto "nota" con los datos del "dto" automáticamente
        notaMapper.updateEntityFromDto(dto, nota);

        // Relaciones
        if (dto.getIdExamen() != null && !dto.getIdExamen().equals(nota.getExamen().getIdExamen())) {
            nota.setExamen(examenRepository.getReferenceById(dto.getIdExamen()));
        }
        if (dto.getIdAlumno() != null && !dto.getIdAlumno().equals(nota.getAlumno().getIdUsuario())) {
            nota.setAlumno(alumnoRepository.getReferenceById(dto.getIdAlumno()));
        }

        return notaMapper.toDto(notaRepository.save(nota));
    }

    @Transactional(readOnly = true)
    public NotaDTO buscarPorId(Integer id) {
        // Se cambia la lógica para que el WHERE activo = true se ejecute en SQL, no en Java
        return notaRepository.findByIdNotaAndActivoTrue(id)
                .map(notaMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada o inactiva"));
    }

    @Transactional(readOnly = true)
    public Page<NotaDTO> listarTodos(Pageable pageable) {
        return notaRepository.findByActivoTrue(pageable).map(notaMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<NotaDTO> buscarPorExamen(Integer idExamen, Pageable pageable) {
        return notaRepository.findByExamen_IdExamenAndActivoTrue(idExamen, pageable).map(notaMapper::toDto);
    }

    @Transactional
    public void eliminarLogico(Integer id) {
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada con ID: " + id));
        nota.setActivo(false);
        // notaRepository.save(nota); <-- No es necesario el save().
        // En @Transactional, JPA detecta que el objeto cambió y hace el UPDATE automáticamente.
    }
}