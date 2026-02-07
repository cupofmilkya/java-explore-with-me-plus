package ru.practicum.web.admin.mapper;

import ru.practicum.web.admin.dto.CompilationDto;
import ru.practicum.web.admin.entity.Compilation;

public class CompilationMapper {

    public static CompilationDto toDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(compilation.getEvents())
                .build();
    }

    public static Compilation toEntity(CompilationDto dto) {
        return Compilation.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .pinned(dto.isPinned())
                .events(dto.getEvents())
                .build();
    }
}