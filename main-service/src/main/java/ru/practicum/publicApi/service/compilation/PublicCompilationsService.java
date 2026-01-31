package ru.practicum.publicApi.service.compilation;

import ru.practicum.base.dto.compilation.CompilationDto;

import java.util.List;

public interface PublicCompilationsService {
    List<CompilationDto> getAll(Boolean pinned, int from, int size);

    CompilationDto get(Long comId);
}
