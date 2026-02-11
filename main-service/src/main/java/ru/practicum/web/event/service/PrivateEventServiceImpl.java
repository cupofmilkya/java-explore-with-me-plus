package ru.practicum.web.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.web.event.dto.*;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.mapper.EventMapper;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.ConflictException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateEventServiceImpl implements PrivateEventService {

    private final EventRepository eventRepository;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventDto addEvent(Long userId, NewEventDto dto) {

        LocalDateTime eventDate = parseDateTime(dto.getEventDate());

        if (eventDate == null ||
                eventDate.isBefore(LocalDateTime.now().plusHours(2))) {

            throw new BadRequestException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
                            + dto.getEventDate()
            );
        }

        Event event = EventMapper.fromNewDto(dto);
        event.setEventDate(eventDate);
        event.setStatus(Event.Status.PENDING);

        Event saved = eventRepository.save(event);
        return EventMapper.toDto(saved);
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, int from, int size) {

        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }

        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findByInitiatorId(userId, pageable)
                .stream()
                .map(EventMapper::toShortDto)
                .toList();
    }

    @Override
    public EventDto updateEvent(Long userId,
                                Long eventId,
                                UpdateEventUserRequest updateRequest) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() ->
                        new BadRequestException("Event not found"));

        if (event.getStatus() == Event.Status.PUBLISHED) {
            throw new ConflictException(
                    "Cannot update published event"
            );
        }

        if (updateRequest.getEventDate() != null) {

            LocalDateTime newEventDate =
                    parseDateTime(updateRequest.getEventDate());

            if (newEventDate == null ||
                    newEventDate.isBefore(LocalDateTime.now().plusHours(2))) {

                throw new BadRequestException(
                        "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
                                + updateRequest.getEventDate()
                );
            }

            event.setEventDate(newEventDate);
        }

        if (updateRequest.getParticipantLimit() != null) {

            if (updateRequest.getParticipantLimit() < 0) {
                throw new BadRequestException(
                        "Participant limit must be non-negative"
                );
            }

            event.setParticipantLimit(
                    updateRequest.getParticipantLimit()
            );
        }

        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        Event saved = eventRepository.save(event);

        return EventMapper.toDto(saved);
    }

    @Override
    public EventDto getEvent(Long userId, Long eventId) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() ->
                        new BadRequestException("Event not found"));

        return EventMapper.toDto(event);
    }

    private LocalDateTime parseDateTime(String date) {

        if (date == null) {
            return null;
        }

        try {
            return LocalDateTime.parse(date, FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException(
                    "Invalid date format. Expected: yyyy-MM-dd HH:mm:ss"
            );
        }
    }
}