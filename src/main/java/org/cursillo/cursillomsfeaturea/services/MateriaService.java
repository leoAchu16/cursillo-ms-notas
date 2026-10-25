package org.cursillo.cursillomsfeaturea.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.MateriaDTO;
import org.cursillo.commons.entities.Materias.Materia;
import org.cursillo.cursillomsfeaturea.mapper.MateriaMapper;
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
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final ProfesorRepository profesorRepository;
    private final MateriaMapper materiaMapper;

    // Inyectamos el CacheManager para el manejo de listas paginadas
    private final CacheManager cacheManager;

    @Transactional
    @CachePut(value = "notas:categoria", key = "#result.idMateria")
    public MateriaDTO crear(MateriaDTO dto) {
        log.info("Iniciando creación de materia con nombre: {}", dto.getNombreMateria());

        Materia materia = materiaMapper.toEntity(dto);

        // Asignar la referencia real de JPA para el Profesor
        materia.setProfesor(profesorRepository.getReferenceById(dto.getIdProfesor()));
        materia.setActivo(true);

        materia = materiaRepository.save(materia);

        MateriaDTO dtoCreado = materiaMapper.toDto(materia);
        log.info("Materia creada exitosamente con ID: {}", materia.getIdMateria());
        log.info("[CACHE] PUT -> notas:categoria:{}", dtoCreado.getIdMateria());

        return dtoCreado;
    }

    @Transactional
    @CachePut(value = "notas:categoria", key = "#id")
    public MateriaDTO actualizar(Integer id, MateriaDTO dto) {
        log.info("Actualizando materia con ID: {}", id);

        Materia materia = materiaRepository.findByIdMateriaAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Materia no encontrada con ID: " + id));

        materiaMapper.updateEntityFromDto(dto, materia);

        if (dto.getIdProfesor() != null && !dto.getIdProfesor().equals(materia.getProfesor().getIdUsuario())) {
            materia.setProfesor(profesorRepository.getReferenceById(dto.getIdProfesor()));
        }

        MateriaDTO dtoActualizado = materiaMapper.toDto(materiaRepository.save(materia));
        log.info("[CACHE] PUT -> notas:categoria:{} (Actualización)", id);

        return dtoActualizado;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "notas:categoria", key = "#id")
    public MateriaDTO buscarPorId(Integer id) {
        log.info("[CACHE] MISS -> Buscando materia ID: {} en BD", id);
        return materiaRepository.findByIdMateriaAndActivoTrue(id)
                .map(materiaMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Materia no encontrada o inactiva con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<MateriaDTO> listarTodos(Pageable pageable) {
        log.info("Listando materias con paginación");
        Page<MateriaDTO> pagina = materiaRepository.findByActivoTrue(pageable).map(materiaMapper::toDto);
        cachearPaginaIndividualmente(pagina);
        return pagina;
    }

    @Transactional(readOnly = true)
    public Page<MateriaDTO> buscarPorNombre(String nombre, Pageable pageable) {
        log.info("Buscando materias por nombre: {}", nombre);
        Page<MateriaDTO> pagina = materiaRepository.findByNombreMateriaContainingIgnoreCaseAndActivoTrue(nombre, pageable)
                .map(materiaMapper::toDto);
        cachearPaginaIndividualmente(pagina);
        return pagina;
    }

    @Transactional(readOnly = true)
    public Page<MateriaDTO> buscarPorProfesor(Integer idProfesor, Pageable pageable) {
        log.info("Buscando materias activas para el profesor ID: {}", idProfesor);
        Page<MateriaDTO> pagina = materiaRepository.findByProfesor_IdUsuarioAndActivoTrue(idProfesor, pageable)
                .map(materiaMapper::toDto);
        cachearPaginaIndividualmente(pagina);
        return pagina;
    }

    @Transactional
    @CacheEvict(value = "notas:categoria", key = "#id")
    public void eliminarLogico(Integer id) {
        log.info("Ejecutando borrado lógico para la materia ID: {}", id);
        Materia materia = materiaRepository.findByIdMateriaAndActivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Materia no encontrada con ID: " + id));

        materia.setActivo(false);
        log.info("Materia ID: {} marcada inactiva. [CACHE] EVICT -> eliminando de notas:categoria", id);
    }

    /**
     * Método auxiliar para extraer elementos de Page<T> y cachearlos de forma individual,
     * respetando la restricción de no guardar objetos completos de paginación.
     */
    private void cachearPaginaIndividualmente(Page<MateriaDTO> pagina) {
        Cache cache = cacheManager.getCache("notas:categoria");
        if (cache != null && pagina.hasContent()) {
            pagina.getContent().forEach(dto -> cache.put(dto.getIdMateria(), dto));
            log.info("[CACHE] PUT MASIVO -> {} materias cacheadas individualmente", pagina.getNumberOfElements());
        }
    }
}