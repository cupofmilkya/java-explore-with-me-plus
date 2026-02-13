package ru.practicum.web.request.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.exception.NotFoundException;
import ru.practicum.web.request.entity.ParticipationRequest;
import ru.practicum.web.request.repository.ParticipationRequestRepository;
import ru.practicum.web.user.entity.User;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final ParticipationRequestRepository requestRepository;

    public void validateAddRequest(User user, Event event, Long userId, Long eventId) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot participate in his own event");
        }
        if (event.getStatus() != Event.Status.PUBLISHED) {
            throw new ConflictException("Event is not published");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        int confirmedRequests = requestRepository.countByEventIdAndStatus(
                eventId, ParticipationRequest.RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }
    }

    public ParticipationRequest validateAndGetRequestForCancellation(Long requestId, Long userId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request not found");
        }
        return request;
    }

    public void validateAndCheckEventOwnership(Event event, Long userId) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + event.getId() + " was not found for user " + userId);
        }
    }

    public void validateEventForRequestUpdate(Event event) {
        if (event.getParticipantLimit() == 0) {
            throw new ConflictException("The participant limit has been reached");
        }
        if (!event.getRequestModeration()) {
            throw new ConflictException("The participant limit has been reached");
        }
    }

    public List<ParticipationRequest> validateAndGetRequestsForUpdate(
            List<Long> requestIds, Long eventId, String status) {

        List<ParticipationRequest> requests = requestRepository.findAllById(requestIds);
        if (requests.isEmpty()) {
            throw new ConflictException("Request ids list is empty");
        }

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new NotFoundException("Request with id=" + request.getId() + " not found for this event");
            }
            if (request.getStatus() != ParticipationRequest.RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }
        return requests;
    }

    public int checkAvailableSlots(Event event) {
        int confirmedRequests = requestRepository.countByEventIdAndStatus(
                event.getId(), ParticipationRequest.RequestStatus.CONFIRMED);
        int participantLimit = event.getParticipantLimit();

        if (confirmedRequests >= participantLimit) {
            throw new ConflictException("The participant limit has been reached");
        }
        return participantLimit - confirmedRequests;
    }
}