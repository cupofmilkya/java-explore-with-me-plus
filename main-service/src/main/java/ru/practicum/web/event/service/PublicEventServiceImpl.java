package ru.practicum.web.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.NotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventDto getEvent(Long id) {
        Event event = eventRepository.findByIdAndStatus(id, Event.Status.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        Long views = getViewsForEvent(event);
        event.setViews(views + 1);

        EventDto dto = EventMapper.toDto(event);
        dto.setViews(views + 1);

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
        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        log.info("Getting events with from={}, size={}, page={}", from, size, page);

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        try {
            if (rangeStart != null && !rangeStart.isBlank()) {
                startDateTime = parseDateTime(rangeStart);
            }
            if (rangeEnd != null && !rangeEnd.isBlank()) {
                endDateTime = parseDateTime(rangeEnd);
            }
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss");
        }

        if (startDateTime == null && rangeStart != null && !rangeStart.isBlank()) {
            throw new BadRequestException("Invalid date format for rangeStart");
        }
        if (endDateTime == null && rangeEnd != null && !rangeEnd.isBlank()) {
            throw new BadRequestException("Invalid date format for rangeEnd");
        }

        if (startDateTime == null) {
            startDateTime = LocalDateTime.now();
        }

        Page<Event> eventPage;
        try {
            eventPage = eventRepository.findPublicEventsWithFilters(
                    text,
                    categories,
                    paid,
                    startDateTime,
                    endDateTime,
                    onlyAvailable != null ? onlyAvailable : false,
                    pageable
            );
        } catch (Exception e) {
            log.error("Error in repository call: ", e);
            throw new BadRequestException("Error while filtering events: " + e.getMessage());
        }

        List<Event> events = eventPage.getContent();
        log.info("Found {} events", events.size());

        if (events.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Long> viewsMap = getViewsMap(events);

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    EventShortDto dto = EventMapper.toShortDto(event);
                    dto.setViews(views);
                    dto.setConfirmedRequests(event.getConfirmedRequests() != null ?
                            event.getConfirmedRequests() : 0L);
                    return dto;
                })
                .collect(Collectors.toList());

        // Сортировка
        if (sort != null) {
            try {
                if ("EVENT_DATE".equals(sort)) {
                    result.sort((e1, e2) -> {
                        if (e1.getEventDate() == null || e2.getEventDate() == null) return 0;
                        return e1.getEventDate().compareTo(e2.getEventDate());
                    });
                } else if ("VIEWS".equals(sort)) {
                    result.sort((e1, e2) -> {
                        if (e1.getViews() == null || e2.getViews() == null) return 0;
                        return e1.getViews().compareTo(e2.getViews());
                    });
                }
            } catch (Exception e) {
                log.error("Error during sorting: ", e);
            }
        }

        return result;
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
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));

        try {
            List<ViewStatsDto> stats = statsClient.getStats(
                    start,
                    LocalDateTime.now(),
                    uris,
                    true
            );

            if (stats == null) {
                return Map.of();
            }

            return stats.stream()
                    .filter(stat -> stat != null && stat.getUri() != null)
                    .collect(Collectors.toMap(
                            stat -> extractEventIdFromUri(stat.getUri()),
                            ViewStatsDto::getHits,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.error("Error getting views stats: {}", e.getMessage());
            return events.stream()
                    .collect(Collectors.toMap(
                            Event::getId,
                            ee -> 0L
                    ));
        }
    }

    private Long getViewsForEvent(Event event) {
        if (event == null || event.getId() == null) {
            return 0L;
        }

        try {
            LocalDateTime start = event.getCreatedOn() != null ?
                    event.getCreatedOn() : LocalDateTime.now().minusYears(1);

            String uri = "/events/" + event.getId();

            List<ViewStatsDto> stats = statsClient.getStats(
                    start,
                    LocalDateTime.now(),
                    List.of(uri),
                    true
            );

            if (stats == null || stats.isEmpty()) {
                return 0L;
            }
            return stats.get(0).getHits();
        } catch (Exception e) {
            log.error("Error getting views for event {}: {}", event.getId(), e.getMessage());
            return 0L;
        }
    }

    private Long extractEventIdFromUri(String uri) {
        if (uri == null) {
            return null;
        }
        try {
            String[] parts = uri.split("/");
            if (parts.length > 0) {
                return Long.parseLong(parts[parts.length - 1]);
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting event id from uri: {}", uri, e);
            return null;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeStr);
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDateTime.parse(dateTimeStr.replace(" ", "T"));
                } catch (DateTimeParseException e3) {
                    throw new DateTimeParseException("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss", dateTimeStr, 0);
                }
            }
        }
    }
}