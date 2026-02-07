package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.admin.dto.CompilationDto;
import ru.practicum.web.admin.entity.Compilation;
import ru.practicum.web.admin.mapper.CompilationMapper;
import ru.practicum.web.admin.repository.CompilationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompilationServiceImpl implements AdminCompilationService {

    private final CompilationRepository repository;

    @Override
    public CompilationDto create(CompilationDto dto) {
        Compilation compilation = CompilationMapper.toEntity(dto);
        return CompilationMapper.toDto(repository.save(compilation));
    }

    @Override
    public CompilationDto update(Long id, CompilationDto dto) {
        Compilation compilation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compilation not found with id " + id));
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.isPinned());
        compilation.setEvents(dto.getEvents());
        return CompilationMapper.toDto(repository.save(compilation));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<CompilationDto> getAll(int from, int size) {
        List<Compilation> compilations = repository.findAll();
        int start = Math.min(from, compilations.size());
        int end = Math.min(start + size, compilations.size());
        return compilations.subList(start, end).stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }
}