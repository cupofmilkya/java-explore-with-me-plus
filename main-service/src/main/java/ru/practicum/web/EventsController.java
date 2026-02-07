package ru.practicum.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.web.dto.EventDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Минимальный контроллер событий, который регистрирует просмотры в сервисе статистики.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventsController {

    private static final String APP_NAME = "ewm-main-service";

    private final InMemoryStore store;
    private final StatsClient statsClient;

    @GetMapping("/events")
    public List<EventDto> getAll(HttpServletRequest request) {
        registerHit(request);
        return store.getEvents();
    }

    @GetMapping("/events/{id}")
    public EventDto getById(@PathVariable("id") long id, HttpServletRequest request) {
        registerHit(request);
        return store.getEventById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    private void registerHit(HttpServletRequest request) {
        try {
            EndpointHitDto dto = EndpointHitDto.builder()
                    .app(APP_NAME)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build();
            statsClient.hit(dto);
        } catch (RuntimeException ex) {
            // Не валим бизнес-логику, если сервис статистики недоступен.
            // Для целей тестов нам достаточно попытаться отправить хит.
        }
    }
}
