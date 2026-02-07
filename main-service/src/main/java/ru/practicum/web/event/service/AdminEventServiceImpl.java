package ru.practicum.web.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.repository.EventRepository;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository repository;

    @Override
    public List<EventDto> getPendingEvents() {
        return repository.findByStatus(Event.Status.PENDING)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public EventDto publishEvent(Long id) {
        Event event = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        event.setStatus(Event.Status.PUBLISHED);
        return toDto(repository.save(event));
    }

    @Override
    public EventDto rejectEvent(Long id) {
        Event event = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        event.setStatus(Event.Status.CANCELED);
        return toDto(repository.save(event));
    }

    private EventDto toDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .status(event.getStatus().name())
                .build();
    }
}