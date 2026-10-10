package org.cursillo.cursillomsfeaturea.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.NotaDTO;
import org.cursillo.commons.entities.Enums.EstadoNota;
import org.cursillo.commons.entities.Notas.Nota;
import org.cursillo.cursillomsfeaturea.mapper.NotaMapper;
import org.cursillo.cursillomsfeaturea.repositories.AlumnoRepository;
import org.cursillo.cursillomsfeaturea.repositories.ExamenRepository;
import org.cursillo.cursillomsfeaturea.repositories.NotaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

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

        calcularCalificacionYEstado(nota);

        return notaMapper.toDto(notaRepository.save(nota));
    }

    @Transactional
    public NotaDTO actualizar(Integer id, NotaDTO dto) {
        log.info("Actualizando nota ID: {}", id);

        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada con ID: " + id));

        notaMapper.updateEntityFromDto(dto, nota);

        if (dto.getIdExamen() != null && !dto.getIdExamen().equals(nota.getExamen().getIdExamen())) {
            nota.setExamen(examenRepository.getReferenceById(dto.getIdExamen()));
        }
        if (dto.getIdAlumno() != null && !dto.getIdAlumno().equals(nota.getAlumno().getIdUsuario())) {
            nota.setAlumno(alumnoRepository.getReferenceById(dto.getIdAlumno()));
        }

        calcularCalificacionYEstado(nota);

        return notaMapper.toDto(notaRepository.save(nota));
    }

    @Transactional
    public List<NotaDTO> actualizarMasivo(List<NotaDTO> dtos) {
        log.info("Iniciando actualización masiva de {} notas", dtos.size());

        return dtos.stream()
                .map(dto -> actualizar(dto.getIdNota(), dto))
                .toList();
    }

    @Transactional(readOnly = true)
    public NotaDTO buscarPorId(Integer id) {
        return notaRepository.findByIdNotaAndActivoTrue(id)
                .map(notaMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada o inactiva"));
    }

    @Transactional(readOnly = true)
    public Page<NotaDTO> listarTodos(Pageable pageable) {
        return notaRepository.findByActivoTrue(pageable).map(notaMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<NotaDTO> buscarPorExamen(Integer idExamen, String nombre, Pageable pageable) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            return notaRepository.findByExamen_IdExamenAndAlumno_NombreContainingIgnoreCaseAndActivoTrue(idExamen, nombre, pageable)
                    .map(notaMapper::toDto);
        }

        return notaRepository.findByExamen_IdExamenAndActivoTrue(idExamen, pageable)
                .map(notaMapper::toDto);
    }

    @Transactional
    public void eliminarLogico(Integer id) {
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada con ID: " + id));
        nota.setActivo(false);
    }

    private void calcularCalificacionYEstado(Nota nota) {
        if (nota.getPuntosCorrectos() != null) {
            int totalPuntos = nota.getExamen().getTotalPuntos();

            if (totalPuntos > 0) {
                double porcentaje = ((double) nota.getPuntosCorrectos() / totalPuntos) * 100;

                if (porcentaje >= 90) {
                    nota.setCalificacion(5);
                    nota.setEstadoNota(EstadoNota.APROBADO);
                } else if (porcentaje >= 80) {
                    nota.setCalificacion(4);
                    nota.setEstadoNota(EstadoNota.APROBADO);
                } else if (porcentaje >= 70) {
                    nota.setCalificacion(3);
                    nota.setEstadoNota(EstadoNota.APROBADO);
                } else if (porcentaje >= 60) {
                    nota.setCalificacion(2);
                    nota.setEstadoNota(EstadoNota.APROBADO);
                } else {
                    nota.setCalificacion(1);
                    nota.setEstadoNota(EstadoNota.REPROBADO);
                }
            }
        } else {
            nota.setCalificacion(null);
            nota.setEstadoNota(EstadoNota.AUSENTE);
        }
    }
}