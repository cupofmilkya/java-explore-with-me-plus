package ru.practicum.web.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.repository.EventRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final StatsClient statsClient;

    @Override
    public List<EventDto> getEvents() {
        List<Event> events = eventRepository.findByStatus(Event.Status.PUBLISHED);

        if (events.isEmpty()) {
            return List.of();
        }

        Map<String, Long> viewsMap = getViewsMap(events);

        return events.stream()
                .map(event -> toDto(event, viewsMap))
                .toList();
    }

    @Override
    public EventDto getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .filter(e -> e.getStatus() == Event.Status.PUBLISHED)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Map<String, Long> viewsMap = getViewsMap(List.of(event));

        return toDto(event, viewsMap);
    }

    private Map<String, Long> getViewsMap(List<Event> events) {
        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        LocalDateTime start = events.stream()
                .map(Event::getEventDate)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        var stats = statsClient.getStats(
                start,
                LocalDateTime.now(),
                uris,
                true
        );

        return stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits
                ));
    }

    private EventDto toDto(Event event, Map<String, Long> viewsMap) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .status(event.getStatus().name())
                .views(viewsMap.getOrDefault("/events/" + event.getId(), 0L))
                .build();
    }
}