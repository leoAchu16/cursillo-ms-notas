package org.cursillo.cursillomsfeaturea.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.NotaDTO;
import org.cursillo.cursillomsfeaturea.services.NotaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notas", description = "API para la gestión de las calificaciones de los alumnos")
public class NotaController {

    private final NotaService notaService;

    @PostMapping
    @Operation(summary = "Registrar una nueva nota para un alumno en un examen")
    public ResponseEntity<NotaDTO> crearNota(@RequestBody NotaDTO dto) {
        log.info("Endpoint POST /api/notas llamado");
        return new ResponseEntity<>(notaService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar una nota existente")
    public ResponseEntity<NotaDTO> actualizarNota(@PathVariable Integer id, @RequestBody NotaDTO dto) {
        log.info("Endpoint PUT /api/notas/{} llamado", id);
        return ResponseEntity.ok(notaService.actualizar(id, dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener los detalles de una nota específica")
    public ResponseEntity<NotaDTO> obtenerNota(@PathVariable Integer id) {
        log.info("Endpoint GET /api/notas/{} llamado", id);
        return ResponseEntity.ok(notaService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todas las notas (paginado)")
    public ResponseEntity<Page<NotaDTO>> listarNotas(Pageable pageable) {
        log.info("Endpoint GET /api/notas llamado");
        return ResponseEntity.ok(notaService.listarTodos(pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Filtrar notas por ID de Examen (paginado)")
    public ResponseEntity<Page<NotaDTO>> buscarNotasPorExamen(@RequestParam Integer idExamen, Pageable pageable) {
        log.info("Endpoint GET /api/notas/buscar llamado con idExamen: {}", idExamen);
        return ResponseEntity.ok(notaService.buscarPorExamen(idExamen, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar (borrado lógico) una nota")
    public ResponseEntity<Void> eliminarNota(@PathVariable Integer id) {
        log.info("Endpoint DELETE /api/notas/{} llamado", id);
        notaService.eliminarLogico(id);
        return ResponseEntity.noContent().build();
    }
}