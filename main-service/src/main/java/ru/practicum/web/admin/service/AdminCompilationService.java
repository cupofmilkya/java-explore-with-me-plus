package ru.practicum.web.admin.service;

import ru.practicum.web.admin.dto.CompilationDto;

import java.util.List;

public interface AdminCompilationService {
    CompilationDto create(CompilationDto dto);
    CompilationDto update(Long id, CompilationDto dto);
    void delete(Long id);
    List<CompilationDto> getAll(int from, int size);
}