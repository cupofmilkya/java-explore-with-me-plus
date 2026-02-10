package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.admin.dto.CompilationDto;
import ru.practicum.web.admin.dto.NewCompilationDto;
import ru.practicum.web.admin.entity.UpdateCompilationRequest;
import ru.practicum.web.admin.entity.Compilation;
import ru.practicum.web.admin.mapper.CompilationMapper;
import ru.practicum.web.admin.repository.CompilationRepository;
import ru.practicum.web.exception.NotFoundException;
import ru.practicum.web.event.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompilationServiceImpl implements AdminCompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank() || dto.getTitle().length() > 50) {
            throw new IllegalArgumentException("Title length must be between 1 and 50 characters");
        }

        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.getPinned() != null ? dto.getPinned() : false);

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            for (Long eventId : dto.getEvents()) {
                if (!eventRepository.existsById(eventId)) {
                    throw new NotFoundException("Event with id=" + eventId + " was not found");
                }
            }
            compilation.setEvents(dto.getEvents());
        } else {
            compilation.setEvents(List.of());
        }

        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CompilationDto update(Long id, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));

        if (dto.getTitle() != null) {
            if (dto.getTitle().isBlank() || dto.getTitle().length() > 50) {
                throw new IllegalArgumentException("Title length must be between 1 and 50 characters");
            }
            compilation.setTitle(dto.getTitle());
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null) {
            if (!dto.getEvents().isEmpty()) {
                for (Long eventId : dto.getEvents()) {
                    if (!eventRepository.existsById(eventId)) {
                        throw new NotFoundException("Event with id=" + eventId + " was not found");
                    }
                }
            }
            compilation.setEvents(dto.getEvents());
        }

        Compilation updated = compilationRepository.save(compilation);
        return CompilationMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!compilationRepository.existsById(id)) {
            throw new NotFoundException("Compilation with id=" + id + " was not found");
        }
        compilationRepository.deleteById(id);
    }
}