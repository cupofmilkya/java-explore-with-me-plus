package ru.practicum.web.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.exception.NotFoundException;
import ru.practicum.web.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.web.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.web.request.dto.ParticipationRequestDto;
import ru.practicum.web.request.entity.ParticipationRequest;
import ru.practicum.web.request.mapper.RequestMapperService;
import ru.practicum.web.request.repository.ParticipationRequestRepository;
import ru.practicum.web.request.validation.RequestValidator;
import ru.practicum.web.user.entity.User;
import ru.practicum.web.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrivateRequestServiceImpl implements PrivateRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestValidator validator;
    private final RequestMapperService mapperService;
    private final RequestStatusUpdateService statusUpdateService;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        checkUserExists(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(mapperService::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        validator.validateAddRequest(user, event, userId, eventId);

        ParticipationRequest request = mapperService.createRequest(user, event);
        ParticipationRequest saved = requestRepository.save(request);

        if (saved.getStatus() == ParticipationRequest.RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return mapperService.toDto(saved);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUserExists(userId);

        ParticipationRequest request = validator.validateAndGetRequestForCancellation(requestId, userId);
        request.setStatus(ParticipationRequest.RequestStatus.CANCELED);

        return mapperService.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        checkUserExists(userId);
        Event event = getEventAndCheckOwnership(userId, eventId);

        return requestRepository.findAllByEventId(eventId).stream()
                .map(mapperService::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest statusUpdateRequest
    ) {
        Event event = getEventAndCheckOwnership(userId, eventId);

        validator.validateEventForRequestUpdate(event);

        List<ParticipationRequest> requests = validator.validateAndGetRequestsForUpdate(
                statusUpdateRequest.getRequestIds(), eventId, statusUpdateRequest.getStatus());

        if ("CONFIRMED".equals(statusUpdateRequest.getStatus())) {
            int availableSlots = validator.checkAvailableSlots(event);
            return statusUpdateService.confirmRequests(requests, event, availableSlots);
        } else if ("REJECTED".equals(statusUpdateRequest.getStatus())) {
            return statusUpdateService.rejectRequests(requests);
        } else {
            throw new ConflictException("Invalid status: " + statusUpdateRequest.getStatus());
        }
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event getEventAndCheckOwnership(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        validator.validateAndCheckEventOwnership(event, userId);
        return event;
    }
}