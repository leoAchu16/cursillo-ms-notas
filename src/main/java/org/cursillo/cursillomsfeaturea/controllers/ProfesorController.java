package org.cursillo.cursillomsfeaturea.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cursillo.commons.entities.Usuarios.Profesor;
import org.cursillo.cursillomsfeaturea.repositories.ProfesorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/materias/profesores") // <--- ¡TRUCO AQUÍ! El Gateway ya deja pasar todo lo de /materias
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profesores", description = "API para la gestión de profesores")
public class ProfesorController {

    private final ProfesorRepository profesorRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ALUMNO', 'PROFESOR', 'SECRETARIA')")
    public ResponseEntity<List<Profesor>> listarProfesores() {
        return ResponseEntity.ok(profesorRepository.findAll());
    }
}