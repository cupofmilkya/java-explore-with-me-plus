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
    public List<EventDto> getEvents(List<Long> users,
                                    List<String> states,
                                    List<Long> categories,
                                    String rangeStart,
                                    String rangeEnd,
                                    int from,
                                    int size) {

        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime start = parseDateTime(rangeStart);
        LocalDateTime end = parseDateTime(rangeEnd);

        List<Event.Status> statusEnums = (states == null || states.isEmpty()) ? null :
                states.stream().map(Event.Status::valueOf).toList();

        Page<Event> eventPage = eventRepository.findEventsByAdminFilters(
                (users == null || users.isEmpty()) ? null : users,
                statusEnums,
                (categories == null || categories.isEmpty()) ? null : categories,
                start,
                end,
                pageable
        );

        List<Event> events = eventPage.getContent();

        events.forEach(event -> event.setViews(getViews(event)));

        return events.stream()
                .map(EventMapper::toDto)
                .toList();
    }

    @Override
    public EventDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Event tempEvent = new Event();
        if (updateRequest.getTitle() != null) tempEvent.setTitle(updateRequest.getTitle());
        if (updateRequest.getAnnotation() != null) tempEvent.setAnnotation(updateRequest.getAnnotation());
        if (updateRequest.getDescription() != null) tempEvent.setDescription(updateRequest.getDescription());
        if (updateRequest.getEventDate() != null) tempEvent.setEventDate(parseDateTime(updateRequest.getEventDate()));
        if (updateRequest.getPaid() != null) tempEvent.setPaid(updateRequest.getPaid());
        if (updateRequest.getParticipantLimit() != null) tempEvent.setParticipantLimit(updateRequest.getParticipantLimit());
        if (updateRequest.getRequestModeration() != null) tempEvent.setRequestModeration(updateRequest.getRequestModeration());

        EventMapper.updateEntityFromDto(event, EventMapper.toDto(tempEvent));

        if (updateRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateRequest.getCategory() + " not found")));
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction().toUpperCase()) {
                case "PUBLISH_EVENT" -> {
                    if (event.getStatus() != Event.Status.PENDING)
                        throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getStatus());
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1)))
                        throw new ConflictException("Cannot publish event because event date is too soon");
                    event.setStatus(Event.Status.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case "REJECT_EVENT" -> {
                    if (event.getStatus() == Event.Status.PUBLISHED)
                        throw new ConflictException("Cannot reject already published event");
                    event.setStatus(Event.Status.CANCELED);
                }
                default ->
                        throw new IllegalArgumentException("Invalid state action: " + updateRequest.getStateAction());
            }
        }

        event.setViews(getViews(event));
        return EventMapper.toDto(eventRepository.save(event));
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
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

    private Long getViews(Event event) {
        if (statsClient == null || event.getId() == null) return 0L;
        try {
            LocalDateTime start = event.getCreatedOn() != null ? event.getCreatedOn() : LocalDateTime.now().minusYears(1);
            String uri = "/events/" + event.getId();
            List<ViewStatsDto> stats = statsClient.getStats(start, LocalDateTime.now(), List.of(uri), true);
            return stats.isEmpty() ? 0L : stats.getFirst().getHits();
        } catch (Exception e) {
            return 0L;
        }
    }

    private EventDto updateRequestToEventDto(UpdateEventAdminRequest req) {
        EventDto dto = new EventDto();
        dto.setTitle(req.getTitle());
        dto.setAnnotation(req.getAnnotation());
        dto.setDescription(req.getDescription());
        dto.setEventDate(req.getEventDate());
        dto.setPaid(req.getPaid());
        dto.setParticipantLimit(req.getParticipantLimit());
        dto.setRequestModeration(req.getRequestModeration());
        return dto;
    }
}