package ru.practicum.web.request.service;

import ru.practicum.web.request.dto.RequestDto;
import ru.practicum.web.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.web.request.dto.EventRequestStatusUpdateResult;

import java.util.List;

public interface PrivateRequestService {

    List<RequestDto> getUserRequests(Long userId);

    RequestDto addRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest statusUpdateRequest
    );
}