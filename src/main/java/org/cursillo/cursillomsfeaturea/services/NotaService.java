package org.cursillo.cursillomsfeaturea.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.NotaDTO;
import org.cursillo.commons.entities.Enums.EstadoNota;
import org.cursillo.commons.entities.Notas.Nota;
import org.cursillo.cursillomsfeaturea.mapper.NotaMapper;
import org.cursillo.cursillomsfeaturea.repositories.AlumnoRepository;
import org.cursillo.cursillomsfeaturea.repositories.ExamenRepository;
import org.cursillo.cursillomsfeaturea.repositories.NotaRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotaService {

    private final NotaRepository notaRepository;
    private final ExamenRepository examenRepository;
    private final AlumnoRepository alumnoRepository;
    private final NotaMapper notaMapper;

    // Inyectamos el CacheManager para el manejo de listas y actualizaciones internas
    private final CacheManager cacheManager;

    @Transactional
    @CachePut(value = "notas:detalle", key = "#result.idNota")
    public NotaDTO crear(NotaDTO dto) {
        log.info("Iniciando registro de nota para el alumno ID: {}", dto.getIdAlumno());

        Nota nota = notaMapper.toEntity(dto);
        nota.setExamen(examenRepository.getReferenceById(dto.getIdExamen()));
        nota.setAlumno(alumnoRepository.getReferenceById(dto.getIdAlumno()));
        nota.setActivo(true);

        calcularCalificacionYEstado(nota);

        NotaDTO dtoCreado = notaMapper.toDto(notaRepository.save(nota));
        log.info("[CACHE] PUT -> notas:detalle:{}", dtoCreado.getIdNota());
        return dtoCreado;
    }

    @Transactional
    @CachePut(value = "notas:detalle", key = "#id")
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

        NotaDTO dtoActualizado = notaMapper.toDto(notaRepository.save(nota));
        log.info("[CACHE] PUT -> notas:detalle:{} (Actualización)", id);
        return dtoActualizado;
    }

    @Transactional
    public List<NotaDTO> actualizarMasivo(List<NotaDTO> dtos) {
        log.info("Iniciando actualización masiva de {} notas", dtos.size());

        List<NotaDTO> notasActualizadas = dtos.stream()
                .map(dto -> actualizar(dto.getIdNota(), dto))
                .toList();

        // Actualización programática del caché (Spring AOP no intercepta llamadas internas)
        Cache cache = cacheManager.getCache("notas:detalle");
        if (cache != null) {
            notasActualizadas.forEach(dto -> cache.put(dto.getIdNota(), dto));
            log.info("[CACHE] PUT MASIVO -> {} notas actualizadas manualmente en caché", notasActualizadas.size());
        }

        return notasActualizadas;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "notas:detalle", key = "#id")
    public NotaDTO buscarPorId(Integer id) {
        log.info("[CACHE] MISS -> Buscando nota ID: {} en BD", id);
        return notaRepository.findByIdNotaAndActivoTrue(id)
                .map(notaMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada o inactiva"));
    }

    @Transactional(readOnly = true)
    public Page<NotaDTO> listarTodos(Pageable pageable) {
        log.info("Listando notas con paginación");
        Page<NotaDTO> pagina = notaRepository.findByActivoTrue(pageable).map(notaMapper::toDto);
        cachearPaginaIndividualmente(pagina);
        return pagina;
    }

    @Transactional(readOnly = true)
    public Page<NotaDTO> buscarPorExamen(Integer idExamen, String nombre, Pageable pageable) {
        log.info("Buscando notas del examen ID: {} con filtro de nombre: {}", idExamen, nombre);
        Page<NotaDTO> pagina;
        if (nombre != null && !nombre.trim().isEmpty()) {
            pagina = notaRepository.findByExamen_IdExamenAndAlumno_NombreContainingIgnoreCaseAndActivoTrue(idExamen, nombre, pageable)
                    .map(notaMapper::toDto);
        } else {
            pagina = notaRepository.findByExamen_IdExamenAndActivoTrue(idExamen, pageable)
                    .map(notaMapper::toDto);
        }
        cachearPaginaIndividualmente(pagina);
        return pagina;
    }

    @Transactional
    @CacheEvict(value = "notas:detalle", key = "#id")
    public void eliminarLogico(Integer id) {
        log.info("Ejecutando borrado lógico para la nota ID: {}", id);
        Nota nota = notaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota no encontrada con ID: " + id));
        nota.setActivo(false);
        log.info("Nota ID: {} marcada inactiva. [CACHE] EVICT -> eliminando de notas:detalle", id);
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

    /**
     * Método auxiliar para extraer elementos de Page<T> y cachearlos de forma individual.
     */
    private void cachearPaginaIndividualmente(Page<NotaDTO> pagina) {
        Cache cache = cacheManager.getCache("notas:detalle");
        if (cache != null && pagina.hasContent()) {
            pagina.getContent().forEach(dto -> cache.put(dto.getIdNota(), dto));
            log.info("[CACHE] PUT MASIVO -> {} notas cacheadas individualmente", pagina.getNumberOfElements());
        }
    }
}