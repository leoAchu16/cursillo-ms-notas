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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    // Inyectamos el CacheManager para procesar las listas paginadas manualmente
    private final CacheManager cacheManager;

    @Transactional
    @CachePut(value = "notas:detalle", key = "#result.idExamen")
    public ExamenDTO crear(ExamenDTO dto) {
        log.info("Iniciando creación de examen: {}", dto.getTitulo());

        Examen examen = examenMapper.toEntity(dto);

        if (dto.getIdMateria() != null) {
            examen.setMateria(materiaRepository.getReferenceById(dto.getIdMateria()));
        }
        if (dto.getIdProfesor() != null) {
            examen.setProfesor(profesorRepository.getReferenceById(dto.getIdProfesor()));
        }

        examen.setActivo(true);
        examen = examenRepository.save(examen);

        ExamenDTO dtoCreado = examenMapper.toDto(examen);
        log.info("Examen creado exitosamente con ID: {}", examen.getIdExamen());
        log.info("[CACHE] PUT -> notas:detalle:{}", dtoCreado.getIdExamen());

        return dtoCreado;
    }

    @Transactional
    @CachePut(value = "notas:detalle", key = "#id")
    public ExamenDTO actualizar(Integer id, ExamenDTO dto) {
        log.info("Actualizando examen ID: {}", id);

        Examen examen = examenRepository.findByIdExamenAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Examen no encontrado con ID: " + id));

        examenMapper.updateEntityFromDto(dto, examen);

        if (dto.getIdMateria() != null && (examen.getMateria() == null || !dto.getIdMateria().equals(examen.getMateria().getIdMateria()))) {
            examen.setMateria(materiaRepository.getReferenceById(dto.getIdMateria()));
        }

        if (dto.getIdProfesor() != null && (examen.getProfesor() == null || !dto.getIdProfesor().equals(examen.getProfesor().getIdUsuario()))) {
            examen.setProfesor(profesorRepository.getReferenceById(dto.getIdProfesor()));
        }

        ExamenDTO dtoActualizado = examenMapper.toDto(examenRepository.save(examen));
        log.info("[CACHE] PUT -> notas:detalle:{} (Actualización)", id);

        return dtoActualizado;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "notas:detalle", key = "#id")
    public ExamenDTO buscarPorId(Integer id) {
        // Si entra a este método, significa que no estaba en Redis (MISS).
        log.info("[CACHE] MISS -> Buscando examen ID: {} en BD", id);
        return examenRepository.findByIdExamenAndActivoTrue(id)
                .map(examenMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Examen no encontrado o inactivo con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ExamenDTO> listarTodos(Pageable pageable) {
        log.info("Listando exámenes activos (Paginado)");
        Page<ExamenDTO> pagina = examenRepository.findByActivoTrue(pageable).map(examenMapper::toDto);
        cachearPaginaIndividualmente(pagina);
        return pagina;
    }

    @Transactional(readOnly = true)
    public Page<ExamenDTO> buscarPorTitulo(String titulo, Pageable pageable) {
        log.info("Buscando exámenes por título: {}", titulo);
        Page<ExamenDTO> pagina = examenRepository.findByTituloContainingIgnoreCaseAndActivoTrue(titulo, pageable)
                .map(examenMapper::toDto);
        cachearPaginaIndividualmente(pagina);
        return pagina;
    }

    @Transactional(readOnly = true)
    public Page<ExamenDTO> buscarPorMateria(Integer idMateria, Pageable pageable) {
        log.info("Buscando exámenes activos para la materia ID: {}", idMateria);
        Page<ExamenDTO> pagina = examenRepository.findByMateria_IdMateriaAndActivoTrue(idMateria, pageable)
                .map(examenMapper::toDto);
        cachearPaginaIndividualmente(pagina);
        return pagina;
    }

    @Transactional
    @CacheEvict(value = "notas:detalle", key = "#id")
    public void eliminarLogico(Integer id) {
        log.info("Ejecutando borrado lógico para el examen ID: {}", id);
        Examen examen = examenRepository.findByIdExamenAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Examen no encontrado con ID: " + id));

        examen.setActivo(false);
        log.info("Examen ID: {} marcado inactivo. [CACHE] EVICT -> eliminando de notas:detalle", id);
    }

    /**
     * Método auxiliar para cumplir la regla: "No cachear el resultado completo paginado".
     * Extrae cada DTO de la página y lo guarda en Redis de forma individual.
     */
    private void cachearPaginaIndividualmente(Page<ExamenDTO> pagina) {
        Cache cache = cacheManager.getCache("notas:detalle");
        if (cache != null && pagina.hasContent()) {
            pagina.getContent().forEach(dto -> cache.put(dto.getIdExamen(), dto));
            log.info("[CACHE] PUT MASIVO -> {} exámenes cacheados individualmente", pagina.getNumberOfElements());
        }
    }
}