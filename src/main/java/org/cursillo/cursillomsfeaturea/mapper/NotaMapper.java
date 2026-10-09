package org.cursillo.cursillomsfeaturea.mapper;

import org.cursillo.commons.dto.NotaDTO;
import org.cursillo.commons.entities.Notas.Nota;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface NotaMapper {

    @Mapping(target = "idExamen", source = "examen.idExamen")
    @Mapping(target = "idAlumno", source = "alumno.idUsuario")
    NotaDTO toDto(Nota nota);

    @Mapping(target = "idNota", ignore = true)
    @Mapping(target = "examen", ignore = true)
    @Mapping(target = "alumno", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creacion", ignore = true)
    @Mapping(target = "actualizacion", ignore = true)
    Nota toEntity(NotaDTO dto);

    @Mapping(target = "idNota", ignore = true)
    @Mapping(target = "examen", ignore = true)
    @Mapping(target = "alumno", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "creacion", ignore = true)
    @Mapping(target = "actualizacion", ignore = true)
    void updateEntityFromDto(NotaDTO dto, @MappingTarget Nota nota);

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