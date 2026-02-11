package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.web.admin.entity.UpdateEventAdminRequest;
import ru.practicum.web.admin.repository.CategoryRepository;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.mapper.EventMapper;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.exception.NotFoundException;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    public List<EventDto> getEvents(List<Long> users,
                                    List<String> states,
                                    List<Long> categories,
                                    String rangeStart,
                                    String rangeEnd,
                                    int from,
                                    int size) {

        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime start = parseDateTime(rangeStart);
        LocalDateTime end = parseDateTime(rangeEnd);

        List<Event.Status> statusEnums = null;
        if (states != null && !states.isEmpty()) {
            try {
                statusEnums = states.stream()
                        .map(Event.Status::valueOf)
                        .toList();
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid state value: " + states);
            }
        }

        Page<Event> eventPage = eventRepository.findEventsByAdminFilters(
                users != null && !users.isEmpty() ? users : null,
                statusEnums,
                categories != null && !categories.isEmpty() ? categories : null,
                start,
                end,
                pageable
        );

        List<Event> events = eventPage.getContent();

        events.forEach(event -> {
            Long views = getViews(event);
            event.setViews(views != null ? views : 0L);
        });

        return events.stream()
                .map(EventMapper::toDto)
                .toList();
    }

    @Override
    public EventDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (updateRequest.getTitle() != null) {
            if (updateRequest.getTitle().length() < 3 || updateRequest.getTitle().length() > 120) {
                throw new BadRequestException("Title length must be between 3 and 120 characters");
            }
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getAnnotation() != null) {
            if (updateRequest.getAnnotation().length() < 20 || updateRequest.getAnnotation().length() > 2000) {
                throw new BadRequestException("Annotation length must be between 20 and 2000 characters");
            }
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getDescription() != null) {
            if (updateRequest.getDescription().length() < 20 || updateRequest.getDescription().length() > 7000) {
                throw new BadRequestException("Description length must be between 20 and 7000 characters");
            }
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            LocalDateTime eventDate = parseDateTime(updateRequest.getEventDate());
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + updateRequest.getEventDate());
            }
            event.setEventDate(eventDate);
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            if (updateRequest.getParticipantLimit() < 0) {
                throw new BadRequestException("Participant limit must be non-negative");
            }
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateRequest.getCategory() + " not found")));
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
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
                    throw new BadRequestException("Invalid state action: " + updateRequest.getStateAction());
            }
        }

        Event savedEvent = eventRepository.save(event);
        Long views = getViews(savedEvent);
        savedEvent.setViews(views != null ? views : 0L);

        return EventMapper.toDto(savedEvent);
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeStr);
            } catch (DateTimeParseException e2) {
                throw new BadRequestException("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss or ISO format");
            }
        }
    }

    private Long getViews(Event event) {
        if (statsClient == null || event.getId() == null) {
            return 0L;
        }
        try {
            LocalDateTime start = event.getCreatedOn() != null ?
                    event.getCreatedOn() : LocalDateTime.now().minusYears(1);
            String uri = "/events/" + event.getId();
            List<ViewStatsDto> stats = statsClient.getStats(start, LocalDateTime.now(), List.of(uri), true);
            return stats.isEmpty() ? 0L : stats.getFirst().getHits();
        } catch (Exception e) {
            return 0L;
        }
    }
}