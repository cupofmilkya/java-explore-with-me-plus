package ru.practicum.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.web.admin.service.AdminEventService;
import ru.practicum.web.event.dto.EventDto;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final AdminEventService service;

    @GetMapping("/pending")
    public ResponseEntity<List<EventDto>> getPendingEvents() {
        List<EventDto> pendingEvents = service.getPendingEvents();
        return ResponseEntity.ok(pendingEvents);
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<EventDto> publishEvent(@PathVariable Long id) {
        EventDto published = service.publishEvent(id);
        return ResponseEntity.status(HttpStatus.OK).body(published);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<EventDto> rejectEvent(@PathVariable Long id) {
        EventDto rejected = service.rejectEvent(id);
        return ResponseEntity.status(HttpStatus.OK).body(rejected);
    }
}