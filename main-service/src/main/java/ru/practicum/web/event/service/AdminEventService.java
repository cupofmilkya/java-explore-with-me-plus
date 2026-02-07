package ru.practicum.web.event.service;

import ru.practicum.web.event.dto.EventDto;

import java.util.List;

public interface AdminEventService {
    List<EventDto> getPendingEvents();
    EventDto publishEvent(Long id);
    EventDto rejectEvent(Long id);
}