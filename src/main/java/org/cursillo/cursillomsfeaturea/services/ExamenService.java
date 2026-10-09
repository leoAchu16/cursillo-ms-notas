package org.cursillo.cursillomsfeaturea.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.ExamenDTO;
import org.cursillo.commons.entities.Enums.EstadoExamen;
import org.cursillo.commons.entities.Examenes.Examen;
import org.cursillo.commons.entities.Materias.Materia;
import org.cursillo.commons.entities.Usuarios.Profesor;
import org.cursillo.cursillomsfeaturea.repositories.ExamenRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamenService {

    private final ExamenRepository examenRepository;

    @Transactional
    public ExamenDTO crear(ExamenDTO dto) {
        log.info("Iniciando creación de examen: {}", dto.getTitulo());
        Examen examen = new Examen();
        actualizarDatosEntidad(examen, dto); // Reutilizamos la lógica de mapeo

        examen = examenRepository.save(examen);
        log.info("Examen creado con ID: {}", examen.getIdExamen());
        return mapToDto(examen);
    }

    @Transactional
    public ExamenDTO actualizar(Integer id, ExamenDTO dto) {
        log.info("Actualizando examen ID: {}", id);
        Examen examen = examenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado con ID: " + id));

        actualizarDatosEntidad(examen, dto); // Reutilizamos la lógica de mapeo

        return mapToDto(examenRepository.save(examen));
    }

    @Transactional(readOnly = true)
    public ExamenDTO buscarPorId(Integer id) {
        log.info("Buscando examen ID: {}", id);
        return examenRepository.findById(id)
                .filter(Examen::getActivo)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado o inactivo"));
    }

    @Transactional(readOnly = true)
    public Page<ExamenDTO> listarTodos(Pageable pageable) {
        log.info("Listando exámenes (Paginado)");
        return examenRepository.findByActivoTrue(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<ExamenDTO> buscarPorTitulo(String titulo, Pageable pageable) {
        log.info("Buscando exámenes por título: {}", titulo);
        return examenRepository.findByTituloContainingIgnoreCaseAndActivoTrue(titulo, pageable).map(this::mapToDto);
    }

    @Transactional
    public void eliminarLogico(Integer id) {
        log.info("Borrado lógico de examen ID: {}", id);
        Examen examen = examenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Examen no encontrado con ID: " + id));
        examen.setActivo(false);
        examenRepository.save(examen);
    }

    // ==========================================
    // Mapeos Bidireccionales y Reutilizables
    // ==========================================

    // Este método centraliza el mapeo para evitar el código duplicado ("Duplicated code fragment")
    private void actualizarDatosEntidad(Examen examen, ExamenDTO dto) {
        examen.setTitulo(dto.getTitulo());
        examen.setFecha(dto.getFecha());

        // Asignaciones directas sin validaciones de null redundantes
        examen.setHora(LocalTime.parse(dto.getHora()));
        examen.setEstado(EstadoExamen.valueOf(dto.getEstado().toString()));
        examen.setTotalPuntos(dto.getTotalPuntos());

        Materia materia = new Materia();
        materia.setIdMateria(dto.getIdMateria());
        examen.setMateria(materia);

        Profesor profesor = new Profesor();
        profesor.setIdUsuario(dto.getIdProfesor());
        examen.setProfesor(profesor);
    }

    private ExamenDTO mapToDto(Examen examen) {
        ExamenDTO dto = new ExamenDTO();
        dto.setIdExamen(examen.getIdExamen());
        dto.setTitulo(examen.getTitulo());
        dto.setFecha(examen.getFecha());

        // Convertimos el objeto LocalTime a texto para enviarlo por la API
        if (examen.getHora() != null) {
            dto.setHora(examen.getHora().toString());
        }

        dto.setTotalPuntos(examen.getTotalPuntos());

        if (examen.getMateria() != null) {
            dto.setIdMateria(examen.getMateria().getIdMateria());
        }
        if (examen.getProfesor() != null) {
            dto.setIdProfesor(examen.getProfesor().getIdUsuario());
        }

        return dto;
    }
}