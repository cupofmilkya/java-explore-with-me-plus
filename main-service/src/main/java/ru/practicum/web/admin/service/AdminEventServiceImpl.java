package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.web.admin.entity.UpdateEventAdminRequest;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.admin.repository.CategoryRepository;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.NotFoundException;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<EventDto> getEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            String rangeStart,
            String rangeEnd,
            int from,
            int size
    ) {
        Pageable pageable = PageRequest.of(from / size, size);

        LocalDateTime start = parseDateTime(rangeStart);
        LocalDateTime end = parseDateTime(rangeEnd);

        Page<Event> eventPage = eventRepository.findEventsByAdminFilters(
                users,
                states,
                categories,
                start,
                end,
                pageable
        );

        List<Event> events = eventPage.getContent();

        return events.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public EventDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime newEventDate = parseDateTime(updateRequest.getEventDate());
            event.setEventDate(newEventDate);
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case "PUBLISH_EVENT":
                    if (event.getStatus() != Event.Status.PENDING) {
                        throw new IllegalStateException("Cannot publish event because it's not in the right state: " + event.getStatus());
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new IllegalStateException("Cannot publish event because event date is too soon");
                    }
                    event.setStatus(Event.Status.PUBLISHED);
                    break;

                case "REJECT_EVENT":
                    if (event.getStatus() == Event.Status.PUBLISHED) {
                        throw new IllegalStateException("Cannot reject already published event");
                    }
                    event.setStatus(Event.Status.CANCELED);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid state action: " + updateRequest.getStateAction());
            }
        }

        Event updated = eventRepository.save(event);
        return toDto(updated);
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
                throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss or ISO format");
            }
        }
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
        if (statsClient == null) {
            return 0L;
        }

        try {
            var stats = statsClient.getStats(
                    event.getEventDate(),
                    LocalDateTime.now(),
                    List.of("/events/" + event.getId()),
                    true
            );

            return stats.isEmpty() ? 0 : stats.getFirst().getHits();
        } catch (Exception e) {
            return 0L;
        }
    }
}