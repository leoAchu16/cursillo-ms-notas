package org.cursillo.cursillomsfeaturea.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.NotaDTO;
import org.cursillo.commons.entities.Enums.EstadoNota;
import org.cursillo.commons.entities.Examenes.Examen;
import org.cursillo.commons.entities.Notas.Nota;
import org.cursillo.commons.entities.Usuarios.Alumno;
import org.cursillo.cursillomsfeaturea.repositories.NotaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotaService {

    private final NotaRepository notaRepository;

    @Transactional
    public NotaDTO crear(NotaDTO dto) {
        log.info("Iniciando registro de nota para el alumno ID: {}", dto.getIdAlumno());
        Nota nota = new Nota();
        actualizarDatosEntidad(nota, dto);

        nota = notaRepository.save(nota);
        log.info("Nota registrada con ID: {}", nota.getIdNota());
        return mapToDto(nota);
    }

    @Transactional
    public NotaDTO actualizar(Integer id, NotaDTO dto) {
        log.info("Actualizando nota ID: {}", id);
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada con ID: " + id));

        actualizarDatosEntidad(nota, dto);

        return mapToDto(notaRepository.save(nota));
    }

    @Transactional(readOnly = true)
    public NotaDTO buscarPorId(Integer id) {
        log.info("Buscando nota ID: {}", id);
        return notaRepository.findById(id)
                .filter(Nota::getActivo)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada o inactiva"));
    }

    @Transactional(readOnly = true)
    public Page<NotaDTO> listarTodos(Pageable pageable) {
        log.info("Listando notas (Paginado)");
        return notaRepository.findByActivoTrue(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<NotaDTO> buscarPorExamen(Integer idExamen, Pageable pageable) {
        log.info("Buscando notas para el examen ID: {}", idExamen);
        return notaRepository.findByExamen_IdExamenAndActivoTrue(idExamen, pageable).map(this::mapToDto);
    }

    @Transactional
    public void eliminarLogico(Integer id) {
        log.info("Borrado lógico de la nota ID: {}", id);
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota no encontrada con ID: " + id));
        nota.setActivo(false);
        notaRepository.save(nota);
    }

    // ==========================================
    // Mapeos Bidireccionales
    // ==========================================
    private void actualizarDatosEntidad(Nota nota, NotaDTO dto) {
        nota.setPuntosCorrectos(dto.getPuntosCorrectos());
        nota.setCalificacion(dto.getCalificacion());

        if (dto.getEstadoNota() != null) {
            nota.setEstadoNota(EstadoNota.valueOf(dto.getEstadoNota().toString()));
        }

        // Relación con Examen
        Examen examen = new Examen();
        examen.setIdExamen(dto.getIdExamen());
        nota.setExamen(examen);

        // Relación con Alumno
        Alumno alumno = new Alumno();
        alumno.setIdUsuario(dto.getIdAlumno());
        nota.setAlumno(alumno);
    }

    private NotaDTO mapToDto(Nota nota) {
        NotaDTO dto = new NotaDTO();
        dto.setIdNota(nota.getIdNota());
        dto.setPuntosCorrectos(nota.getPuntosCorrectos());
        dto.setCalificacion(nota.getCalificacion());

        if (nota.getExamen() != null) {
            dto.setIdExamen(nota.getExamen().getIdExamen());
        }
        if (nota.getAlumno() != null) {
            dto.setIdAlumno(nota.getAlumno().getIdUsuario());
        }

        return dto;
    }
}