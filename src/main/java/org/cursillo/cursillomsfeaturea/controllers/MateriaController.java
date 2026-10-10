package org.cursillo.cursillomsfeaturea.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.MateriaDTO;
import org.cursillo.cursillomsfeaturea.services.MateriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/materias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Materias", description = "API para la gestión de materias académicas")
public class MateriaController {

    private final MateriaService materiaService;

    @PostMapping
    @Operation(summary = "Crear una nueva materia")
    public ResponseEntity<MateriaDTO> crearMateria(@RequestBody MateriaDTO dto) {
        log.info("Petición REST para crear materia recibida");
        MateriaDTO nuevaMateria = materiaService.crear(dto);
        return new ResponseEntity<>(nuevaMateria, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una materia existente por ID")
    public ResponseEntity<MateriaDTO> actualizarMateria(@PathVariable Integer id, @RequestBody MateriaDTO dto) {
        log.info("Petición REST para actualizar materia ID: {}", id);
        return ResponseEntity.ok(materiaService.actualizar(id, dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una materia específica por su ID")
    public ResponseEntity<MateriaDTO> obtenerMateriaPorId(@PathVariable Integer id) {
        log.info("Petición REST para obtener materia ID: {}", id);
        return ResponseEntity.ok(materiaService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todas las materias activas (con paginación)")
    public ResponseEntity<Page<MateriaDTO>> listarMaterias(Pageable pageable) {
        log.info("Petición REST para listar materias");
        return ResponseEntity.ok(materiaService.listarTodos(pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar materias por nombre (con filtros y paginación)")
    public ResponseEntity<Page<MateriaDTO>> buscarMateriasPorNombre(
            @RequestParam String nombre,
            Pageable pageable) {
        log.info("Petición REST para buscar materias con el filtro: {}", nombre);
        return ResponseEntity.ok(materiaService.buscarPorNombre(nombre, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar (borrado lógico) una materia")
    public ResponseEntity<Void> eliminarMateria(@PathVariable Integer id) {
        log.info("Petición REST para eliminar materia ID: {}", id);
        materiaService.eliminarLogico(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/profesor/{idProfesor}")
    @Operation(summary = "Listar todas las materias dictadas por un profesor específico (paginado)")
    public ResponseEntity<Page<MateriaDTO>> listarMateriasPorProfesor(
            @PathVariable Integer idProfesor,
            Pageable pageable) {
        log.info("Petición REST para listar materias del profesor ID: {}", idProfesor);
        return ResponseEntity.ok(materiaService.buscarPorProfesor(idProfesor, pageable));
    }

}