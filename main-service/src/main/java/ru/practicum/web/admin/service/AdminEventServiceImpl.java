package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.dto.ViewStatsDto; // Импортируем ваш DTO
import ru.practicum.web.admin.entity.UpdateEventAdminRequest;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.admin.repository.CategoryRepository;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.ConflictException;
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

        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime newEventDate = parseDateTime(updateRequest.getEventDate());
            event.setEventDate(newEventDate);
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getCategory() != null) {
            categoryRepository.findById(updateRequest.getCategory())
                    .ifPresent(event::setCategory);
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction().toUpperCase()) {
                case "PUBLISH_EVENT":
                    if (event.getStatus() != Event.Status.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getStatus());
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Cannot publish event because event date is too soon");
                    }
                    event.setStatus(Event.Status.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;

                case "REJECT_EVENT":
                    if (event.getStatus() == Event.Status.PUBLISHED) {
                        throw new ConflictException("Cannot reject already published event");
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
        EventDto result = EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .eventDate(event.getEventDate() != null ? event.getEventDate().format(FORMATTER) : null)
                .paid(event.getPaid() != null ? event.getPaid() : false)
                .participantLimit(event.getParticipantLimit() != null ? event.getParticipantLimit() : 0)
                .requestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true)
                .state(event.getStatus() != null ? event.getStatus().name() : null)
                .views(getViews(event))
                .confirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0L)
                .build();

        if (event.getCreatedOn() != null) {
            result.setCreatedOn(event.getCreatedOn().format(FORMATTER));
        }

        if (event.getPublishedOn() != null) {
            result.setPublishedOn(event.getPublishedOn().format(FORMATTER));
        }

        return result;
    }

    private Long getViews(Event event) {
        if (statsClient == null || event.getId() == null) {
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

            return stats.isEmpty() ? 0L : stats.getFirst().getHits();
        } catch (Exception e) {
            return 0L;
        }
    }
}