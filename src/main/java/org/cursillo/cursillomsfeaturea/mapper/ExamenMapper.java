package org.cursillo.cursillomsfeaturea.mapper;

import org.cursillo.commons.dto.ExamenDTO;
import org.cursillo.commons.entities.Examenes.Examen;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface ExamenMapper {

    @Mapping(target = "idMateria", source = "materia.idMateria")
    @Mapping(target = "idProfesor", source = "profesor.idUsuario")
    @Mapping(target = "creacion", source = "creacion")
    @Mapping(target = "actualizacion", source = "actualizacion")
    ExamenDTO toDto(Examen examen);

    @Mapping(target = "idExamen", ignore = true)
    @Mapping(target = "materia", ignore = true)
    @Mapping(target = "profesor", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creacion", ignore = true)
    @Mapping(target = "actualizacion", ignore = true)
    Examen toEntity(ExamenDTO dto);

    @Mapping(target = "idExamen", ignore = true)
    @Mapping(target = "materia", ignore = true)
    @Mapping(target = "profesor", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creacion", ignore = true)
    @Mapping(target = "actualizacion", ignore = true)
    void updateEntityFromDto(ExamenDTO dto, @MappingTarget Examen examen);

    // =========================================================================
    // Métodos de conversión personalizada entre LocalDateTime y OffsetDateTime
    // =========================================================================

    default OffsetDateTime map(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atOffset(ZoneOffset.UTC);
    }

    default LocalDateTime map(OffsetDateTime value) {
        if (value == null) {
            return null;
        }
        return value.toLocalDateTime();
    }
}