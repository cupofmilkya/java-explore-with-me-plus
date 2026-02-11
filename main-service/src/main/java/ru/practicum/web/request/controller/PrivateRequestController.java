package ru.practicum.web.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.web.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.web.request.dto.RequestDto;
import ru.practicum.web.request.service.PrivateRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateRequestController {

    private final PrivateRequestService requestService;

    @GetMapping("/users/{userId}/requests")
    public ResponseEntity<List<RequestDto>> getUserRequests(@PathVariable Long userId) {
        return ResponseEntity.ok(requestService.getUserRequests(userId));
    }

    @PostMapping("/users/{userId}/requests")
    public ResponseEntity<RequestDto> addRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId
    ) {
        RequestDto created = requestService.addRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<RequestDto> cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId
    ) {
        return ResponseEntity.ok(requestService.cancelRequest(userId, requestId));
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<RequestDto>> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(requestService.getEventRequests(userId, eventId));
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<Object> updateRequestsStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest
    ) {
        return ResponseEntity.ok(requestService.updateRequestsStatus(userId, eventId, statusUpdateRequest));
    }
}