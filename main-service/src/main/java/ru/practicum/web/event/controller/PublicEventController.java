package ru.practicum.web.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.dto.EventShortDto;
import ru.practicum.web.event.service.PublicEventService;
import ru.practicum.web.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final PublicEventService publicEventService;
    private final StatsClient statsClient;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }
        hitStats(request);
        List<EventShortDto> events = publicEventService.getEvents(
                text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size
        );
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id,
                                             HttpServletRequest request) {
        hitStats(request);
        EventDto event = publicEventService.getEvent(id);
        return ResponseEntity.ok(event);
    }

    private void hitStats(HttpServletRequest request) {
        try {
            statsClient.hit(
                    EndpointHitDto.builder()
                            .app("ewm-main-service")
                            .uri(request.getRequestURI())
                            .ip(request.getRemoteAddr())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        } catch (Exception e) {
            System.err.println("Error sending stats: " + e.getMessage());
        }
    }
}