package ru.practicum.publicApi.service.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.base.dto.event.EventFullDto;
import ru.practicum.base.dto.event.EventShortDto;
import ru.practicum.publicApi.dto.RequestParamForEvent;

import java.util.Set;

public interface PublicEventsService {
    Set<EventShortDto> getAll(RequestParamForEvent param);

    EventFullDto get(Long id, HttpServletRequest request);
}
