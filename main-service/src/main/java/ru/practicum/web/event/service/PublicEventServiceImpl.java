package ru.practicum.web.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.dto.EventShortDto;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.mapper.EventMapper;
import ru.practicum.web.event.repository.EventRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventDto getEvent(Long id) {
        Event event = eventRepository.findById(id)
                .filter(e -> e.getStatus() == Event.Status.PUBLISHED)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Map<Long, Long> viewsMap = getViewsMap(List.of(event));

        EventDto dto = EventMapper.toDto(event);
        if (dto != null) {
            dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
        }
        return dto;
    }

    @Override
    public List<EventShortDto> getEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd,
            Boolean onlyAvailable,
            String sort,
            int from,
            int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size);

        LocalDateTime startDateTime = parseDateTime(rangeStart);
        LocalDateTime endDateTime = parseDateTime(rangeEnd);

        if (startDateTime == null) {
            startDateTime = LocalDateTime.now();
        }

        Page<Event> eventPage = eventRepository.findPublicEventsWithFilters(
                text,
                categories,
                paid,
                startDateTime,
                endDateTime,
                onlyAvailable != null ? onlyAvailable : false,
                pageable
        );

        List<Event> events = eventPage.getContent();

        Map<Long, Long> viewsMap = getViewsMap(events);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = EventMapper.toShortDto(event);
                    if (dto != null) {
                        dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getViewsMap(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());

        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .filter(date -> date != null)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));

        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    start,
                    LocalDateTime.now(),
                    uris,
                    true
            );

            return stats.stream()
                    .filter(stat -> stat.getUri() != null)
                    .collect(Collectors.toMap(
                            stat -> extractEventIdFromUri(stat.getUri()),
                            ViewStatsDto::getHits,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private Long extractEventIdFromUri(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateTimeStr);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}