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
import ru.practicum.web.request.mapper.ParticipationRequestMapper;
import ru.practicum.web.request.repository.ParticipationRequestRepository;
import ru.practicum.web.user.entity.User;
import ru.practicum.web.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PrivateRequestServiceImpl implements PrivateRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
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

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        int confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequest.RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(event.getParticipantLimit() == 0 || !event.getRequestModeration()
                        ? ParticipationRequest.RequestStatus.CONFIRMED
                        : ParticipationRequest.RequestStatus.PENDING)
                .build();

        ParticipationRequest saved = requestRepository.save(request);

        if (saved.getStatus() == ParticipationRequest.RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return ParticipationRequestMapper.toDto(saved);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request not found");
        }

        request.setStatus(ParticipationRequest.RequestStatus.CANCELED);
        ParticipationRequest updated = requestRepository.save(request);

        return ParticipationRequestMapper.toDto(updated);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        return requestRepository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
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
            throw new ConflictException("Participant limit is 0, no moderation needed");
        }

        if (!event.getRequestModeration()) {
            throw new ConflictException("Request moderation is disabled for this event");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(statusUpdateRequest.getRequestIds());

        if (requests.isEmpty()) {
            throw new ConflictException("Request ids list is empty");
        }

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new NotFoundException("Request with id=" + request.getId() + " not found for this event");
            }
        }

        int confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequest.RequestStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit();
        int availableSlots = participantLimit - confirmedRequests;

        List<ParticipationRequestDto> confirmedRequestsList = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequestsList = new ArrayList<>();

        if ("CONFIRMED".equals(statusUpdateRequest.getStatus())) {
            for (ParticipationRequest request : requests) {
                if (request.getStatus() != ParticipationRequest.RequestStatus.PENDING) {
                    throw new ConflictException("Request must have status PENDING");
                }
            }

            int confirmedCount = 0;
            for (ParticipationRequest request : requests) {
                if (confirmedCount < availableSlots) {
                    request.setStatus(ParticipationRequest.RequestStatus.CONFIRMED);
                    confirmedCount++;
                    confirmedRequestsList.add(ParticipationRequestMapper.toDto(request));
                } else {
                    request.setStatus(ParticipationRequest.RequestStatus.REJECTED);
                    rejectedRequestsList.add(ParticipationRequestMapper.toDto(request));
                }
                requestRepository.save(request);
            }

            event.setConfirmedRequests(event.getConfirmedRequests() + confirmedCount);
            eventRepository.save(event);

        } else if ("REJECTED".equals(statusUpdateRequest.getStatus())) {
            for (ParticipationRequest request : requests) {
                if (request.getStatus() != ParticipationRequest.RequestStatus.PENDING) {
                    throw new ConflictException("Request must have status PENDING");
                }
            }

            for (ParticipationRequest request : requests) {
                request.setStatus(ParticipationRequest.RequestStatus.REJECTED);
                requestRepository.save(request);
                rejectedRequestsList.add(ParticipationRequestMapper.toDto(request));
            }
        } else {
            throw new ConflictException("Invalid status: " + statusUpdateRequest.getStatus());
        }

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequestsList)
                .rejectedRequests(rejectedRequestsList)
                .build();
    }
}