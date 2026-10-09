package org.cursillo.cursillomsfeaturea.mapper;

import org.cursillo.commons.dto.MateriaDTO;
import org.cursillo.commons.entities.Materias.Materia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface MateriaMapper {

    @Mapping(target = "idProfesor", source = "profesor.idUsuario")
    MateriaDTO toDto(Materia materia);

    @Mapping(target = "idMateria", ignore = true)
    @Mapping(target = "profesor", ignore = true)
    @Mapping(target = "creacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Materia toEntity(MateriaDTO dto);

    @Mapping(target = "idMateria", ignore = true)
    @Mapping(target = "profesor", ignore = true)
    @Mapping(target = "creacion", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntityFromDto(MateriaDTO dto, @MappingTarget Materia materia);

    default OffsetDateTime map(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    default LocalDateTime map(OffsetDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }
}