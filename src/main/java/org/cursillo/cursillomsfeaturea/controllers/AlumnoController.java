package org.cursillo.cursillomsfeaturea.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.entities.Usuarios.Alumno;
import org.cursillo.cursillomsfeaturea.repositories.AlumnoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
// TRUCO DEL GATEWAY: Si el gateway bloquea "/alumnos", cámbialo a "/materias/alumnos"
// tal como hicimos con los profesores para que deje pasar la petición.
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alumnos", description = "API para la gestión de alumnos")
public class AlumnoController {

    private final AlumnoRepository alumnoRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'SECRETARIA')")
    @Operation(summary = "Listar todos los alumnos")
    public ResponseEntity<List<Alumno>> listarAlumnos() {
        log.info("Petición REST para listar todos los alumnos");
        return ResponseEntity.ok(alumnoRepository.findAll());
    }
}