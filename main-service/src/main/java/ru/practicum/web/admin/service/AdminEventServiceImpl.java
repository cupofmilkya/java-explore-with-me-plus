package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.repository.EventRepository;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository repository;
    private final StatsClient statsClient;

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
                .views(getViews(event))
                .build();
    }

    private long getViews(Event event) {
        var stats = statsClient.getStats(
                event.getEventDate(),
                LocalDateTime.now(),
                List.of("/events/" + event.getId()),
                true
        );

        return stats.isEmpty() ? 0 : stats.getFirst().getHits();
    }
}