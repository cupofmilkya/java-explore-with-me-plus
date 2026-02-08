package ru.practicum.web.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.web.event.dto.EventDto;
import ru.practicum.web.event.service.PublicEventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final PublicEventService publicEventService;
    private final StatsClient statsClient;

    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(HttpServletRequest request) {
        hitStats(request);
        return ResponseEntity.ok(publicEventService.getEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id,
                                             HttpServletRequest request) {
        hitStats(request);
        return ResponseEntity.ok(publicEventService.getEvent(id));
    }

    private void hitStats(HttpServletRequest request) {
        statsClient.hit(
                EndpointHitDto.builder()
                        .app("ewm-main-service")
                        .uri(request.getRequestURI())
                        .ip(request.getRemoteAddr())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}