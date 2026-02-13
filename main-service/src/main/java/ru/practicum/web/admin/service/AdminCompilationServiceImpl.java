package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.admin.dto.CompilationDto;
import ru.practicum.web.admin.dto.NewCompilationDto;
import ru.practicum.web.admin.entity.Compilation;
import ru.practicum.web.admin.entity.UpdateCompilationRequest;
import ru.practicum.web.admin.mapper.CompilationMapper;
import ru.practicum.web.admin.repository.CompilationRepository;
import ru.practicum.web.admin.validation.CompilationValidator;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCompilationServiceImpl implements AdminCompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationValidator validator;

    @Override
    public CompilationDto create(NewCompilationDto dto) {
        validator.validateCreateRequest(dto);

        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.getPinned() != null ? dto.getPinned() : false);
        compilation.setEvents(getEventsFromIds(dto.getEvents()));

        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.toDto(saved);
    }

    @Override
    public CompilationDto update(Long id, UpdateCompilationRequest dto) {
        validator.validateUpdateRequest(dto);

        Compilation compilation = getCompilationOrThrow(id);

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            compilation.setEvents(getEventsFromIds(dto.getEvents()));
        }

        Compilation updated = compilationRepository.save(compilation);
        return CompilationMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        validator.validateCompilationExists(compilationRepository.existsById(id), id);
        compilationRepository.deleteById(id);
    }

    private Compilation getCompilationOrThrow(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));
    }

    private List<Event> getEventsFromIds(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Event> events = eventRepository.findAllById(eventIds);
        if (events.size() != eventIds.size()) {
            List<Long> foundIds = events.stream().map(Event::getId).collect(Collectors.toList());
            List<Long> notFoundIds = eventIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new NotFoundException("Events with ids=" + notFoundIds + " not found");
        }
        return events;
    }
}