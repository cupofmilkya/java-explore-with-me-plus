package ru.practicum.web.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.exception.NotFoundException;
import ru.practicum.web.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.web.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.web.request.dto.RequestDto;
import ru.practicum.web.request.entity.Request;
import ru.practicum.web.request.mapper.RequestMapper;
import ru.practicum.web.request.repository.RequestRepository;
import ru.practicum.web.user.entity.User;
import ru.practicum.web.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrivateRequestServiceImpl implements PrivateRequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<RequestDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot participate in his own event");
        }

        if (event.getStatus() != Event.Status.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Request already exists");
        }

        Long confirmedRequests = requestRepository.countConfirmedRequests(eventId);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }

        Request request = Request.builder()
                .created(LocalDateTime.now().withNano(0))
                .event(event)
                .requester(user)
                .status(event.getParticipantLimit() == 0 ? Request.Status.CONFIRMED : Request.Status.PENDING)
                .build();

        Request saved = requestRepository.save(request);

        if (event.getParticipantLimit() == 0) {
            event.setConfirmedRequests(confirmedRequests + 1);
            eventRepository.save(event);
        }

        return RequestMapper.toDto(saved);
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request not found");
        }

        request.setStatus(Request.Status.CANCELED);
        Request updated = requestRepository.save(request);

        return RequestMapper.toDto(updated);
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest statusUpdateRequest
    ) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getParticipantLimit() == 0) {
            throw new ConflictException("Request moderation is disabled for this event");
        }

        if (!event.getRequestModeration()) {
            throw new ConflictException("Request moderation is disabled for this event");
        }

        List<Request> requests = requestRepository.findAllById(statusUpdateRequest.getRequestIds());

        if (requests.isEmpty()) {
            throw new BadRequestException("Request ids list is empty");
        }

        for (Request request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new NotFoundException("Request with id=" + request.getId() + " not found for this event");
            }
            if (request.getStatus() != Request.Status.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        Long confirmedRequests = requestRepository.countConfirmedRequests(eventId);
        int participantLimit = event.getParticipantLimit();

        List<RequestDto> confirmedRequestsList = new ArrayList<>();
        List<RequestDto> rejectedRequestsList = new ArrayList<>();

        if ("CONFIRMED".equals(statusUpdateRequest.getStatus())) {
            for (Request request : requests) {
                if (confirmedRequests < participantLimit) {
                    request.setStatus(Request.Status.CONFIRMED);
                    confirmedRequests++;
                    confirmedRequestsList.add(RequestMapper.toDto(request));
                } else {
                    request.setStatus(Request.Status.REJECTED);
                    rejectedRequestsList.add(RequestMapper.toDto(request));
                }
                requestRepository.save(request);
            }

            event.setConfirmedRequests(confirmedRequests);
            eventRepository.save(event);

        } else if ("REJECTED".equals(statusUpdateRequest.getStatus())) {
            for (Request request : requests) {
                request.setStatus(Request.Status.REJECTED);
                requestRepository.save(request);
                rejectedRequestsList.add(RequestMapper.toDto(request));
            }
        } else {
            throw new BadRequestException("Invalid status: " + statusUpdateRequest.getStatus());
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequestsList)
                .rejectedRequests(rejectedRequestsList)
                .build();
    }
}