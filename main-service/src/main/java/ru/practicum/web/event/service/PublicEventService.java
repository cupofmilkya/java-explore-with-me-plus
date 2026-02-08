package ru.practicum.web.event.service;

import ru.practicum.web.event.dto.EventDto;

import java.util.List;

public interface PublicEventService {

    List<EventDto> getEvents();

    EventDto getEvent(Long id);
}