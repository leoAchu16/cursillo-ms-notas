package org.cursillo.cursillomsfeaturea.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.dto.ExamenDTO;
import org.cursillo.cursillomsfeaturea.services.ExamenService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/examenes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Exámenes", description = "API para la gestión de evaluaciones")
public class ExamenController {

    private final ExamenService examenService;

    @PostMapping
    @Operation(summary = "Programar un nuevo examen")
    public ResponseEntity<ExamenDTO> crearExamen(@RequestBody ExamenDTO dto) {
        log.info("Endpoint POST /api/examenes llamado");
        return new ResponseEntity<>(examenService.crear(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar los datos de un examen")
    public ResponseEntity<ExamenDTO> actualizarExamen(@PathVariable Integer id, @RequestBody ExamenDTO dto) {
        log.info("Endpoint PUT /api/examenes/{} llamado", id);
        return ResponseEntity.ok(examenService.actualizar(id, dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver detalles de un examen por ID")
    public ResponseEntity<ExamenDTO> obtenerExamen(@PathVariable Integer id) {
        log.info("Endpoint GET /api/examenes/{} llamado", id);
        return ResponseEntity.ok(examenService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos los exámenes (paginado)")
    public ResponseEntity<Page<ExamenDTO>> listarExamenes(Pageable pageable) {
        log.info("Endpoint GET /api/examenes llamado");
        return ResponseEntity.ok(examenService.listarTodos(pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Filtrar exámenes por título (paginado)")
    public ResponseEntity<Page<ExamenDTO>> buscarExamenes(@RequestParam String titulo, Pageable pageable) {
        log.info("Endpoint GET /api/examenes/buscar llamado con titulo: {}", titulo);
        return ResponseEntity.ok(examenService.buscarPorTitulo(titulo, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar/Eliminar lógicamente un examen")
    public ResponseEntity<Void> eliminarExamen(@PathVariable Integer id) {
        log.info("Endpoint DELETE /api/examenes/{} llamado", id);
        examenService.eliminarLogico(id);
        return ResponseEntity.noContent().build();
    }
}