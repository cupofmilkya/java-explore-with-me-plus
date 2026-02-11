package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.admin.dto.CompilationDto;
import ru.practicum.web.admin.dto.NewCompilationDto;
import ru.practicum.web.admin.entity.Compilation;
import ru.practicum.web.admin.entity.UpdateCompilationRequest;
import ru.practicum.web.admin.repository.CompilationRepository;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompilationServiceImpl implements AdminCompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto create(NewCompilationDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("Title must not be blank");
        }
        if (dto.getTitle().length() < 1 || dto.getTitle().length() > 50) {
            throw new BadRequestException("Title length must be between 1 and 50 characters");
        }

        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.getPinned() != null ? dto.getPinned() : false);

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            List<Event> events = new ArrayList<>();
            for (Long eventId : dto.getEvents()) {
                Event event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
                events.add(event);
            }
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        Compilation saved = compilationRepository.save(compilation);
        return toDto(saved);
    }

    @Override
    public CompilationDto update(Long id, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));

        if (dto.getTitle() != null) {
            if (dto.getTitle().isBlank()) {
                throw new BadRequestException("Title must not be blank");
            }
            if (dto.getTitle().length() < 1 || dto.getTitle().length() > 50) {
                throw new BadRequestException("Title length must be between 1 and 50 characters");
            }
            compilation.setTitle(dto.getTitle());
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null) {
            List<Event> events = new ArrayList<>();
            for (Long eventId : dto.getEvents()) {
                Event event = eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
                events.add(event);
            }
            compilation.setEvents(events);
        }

        Compilation updated = compilationRepository.save(compilation);
        return toDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (!compilationRepository.existsById(id)) {
            throw new NotFoundException("Compilation with id=" + id + " was not found");
        }
        compilationRepository.deleteById(id);
    }

    private CompilationDto toDto(Compilation compilation) {
        List<Event> events = compilation.getEvents() != null ?
                new ArrayList<>(compilation.getEvents()) :
                new ArrayList<>();

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }
}