package org.cursillo.cursillomsfeaturea.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.MateriaDTO; // <-- ¡OJO! Si te marca rojo, revisa el nombre exacto de tu DTO generado
import org.cursillo.commons.entities.Enums.DiasSemana;
import org.cursillo.commons.entities.Materias.Materia;
import org.cursillo.commons.entities.Usuarios.Profesor;
import org.cursillo.cursillomsfeaturea.repositories.MateriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j // Anotación de Lombok para cumplir con el requisito de Logs
public class MateriaService {

    private final MateriaRepository materiaRepository;

    @Transactional
    public MateriaDTO crear(MateriaDTO dto) {
        log.info("Iniciando creación de materia con nombre: {}", dto.getNombreMateria());

        Materia materia = mapToEntity(dto);
        materia = materiaRepository.save(materia);

        log.info("Materia creada exitosamente con ID: {}", materia.getIdMateria());
        return mapToDto(materia);
    }

    @Transactional
    public MateriaDTO actualizar(Integer id, MateriaDTO dto) {
        log.info("Actualizando materia con ID: {}", id);

        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + id));

        materia.setNombreMateria(dto.getNombreMateria());
        // Ajusta el tipo de dato de DiaSemana si OpenAPI lo generó como String
        materia.setDia(DiasSemana.valueOf(dto.getDia().toString()));
        materia.setDescripcion(dto.getDescripcion());

        materia = materiaRepository.save(materia);
        return mapToDto(materia);
    }

    @Transactional(readOnly = true)
    public MateriaDTO buscarPorId(Integer id) {
        log.info("Buscando materia por ID: {}", id);
        Materia materia = materiaRepository.findById(id)
                .filter(Materia::getActivo) // Solo devuelve si no está borrada lógicamente
                .orElseThrow(() -> new RuntimeException("Materia no encontrada o inactiva"));
        return mapToDto(materia);
    }

    @Transactional(readOnly = true)
    public Page<MateriaDTO> listarTodos(Pageable pageable) {
        log.info("Listando materias con paginación");
        return materiaRepository.findByActivoTrue(pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<MateriaDTO> buscarPorNombre(String nombre, Pageable pageable) {
        log.info("Buscando materias por nombre: {}", nombre);
        return materiaRepository.findByNombreMateriaContainingIgnoreCaseAndActivoTrue(nombre, pageable).map(this::mapToDto);
    }

    @Transactional
    public void eliminarLogico(Integer id) {
        log.info("Ejecutando borrado lógico para la materia ID: {}", id);
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Materia no encontrada con ID: " + id));

        materia.setActivo(false); // Cumple con el requisito de borrado lógico
        materiaRepository.save(materia);
        log.info("Materia ID: {} marcada como inactiva", id);
    }

    // =======================================================
    // REQUISITO: Implementar conversión bidireccional DTO/Entity
    // =======================================================
    private Materia mapToEntity(MateriaDTO dto) {
        Materia materia = new Materia();
        materia.setNombreMateria(dto.getNombreMateria());

        // Ajusta esto dependiendo de cómo OpenAPI generó el Enum
        materia.setDia(DiasSemana.valueOf(dto.getDia().toString()));
        materia.setDescripcion(dto.getDescripcion());

        // Mapeo de la relación: Solo seteamos el ID del profesor
        Profesor profesor = new Profesor();
        profesor.setIdUsuario(dto.getIdProfesor());
        materia.setProfesor(profesor);

        return materia;
    }

    private MateriaDTO mapToDto(Materia materia) {
        MateriaDTO dto = new MateriaDTO();
        dto.setIdMateria(materia.getIdMateria());
        dto.setNombreMateria(materia.getNombreMateria());

        // Ajusta dependiendo de cómo OpenAPI generó el Enum (ej: si usó un enum interno)
        // dto.setDia(MateriaDTO.DiaEnum.fromValue(materia.getDia().name()));
        // Por simplicidad si lo generó como String u Object, usa .name() o valor directo

        dto.setDescripcion(materia.getDescripcion());

        if (materia.getProfesor() != null) {
            dto.setIdProfesor(materia.getProfesor().getIdUsuario());
        }

        return dto;
    }
}