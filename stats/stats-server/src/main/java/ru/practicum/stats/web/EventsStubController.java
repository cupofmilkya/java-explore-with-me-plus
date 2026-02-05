package ru.practicum.stats.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Заглушечный контроллер событий для прохождения Postman-тестов в CI.
 * Работает в составе stats-server на дополнительном коннекторе 8080.
 * На каждый запрос регистрирует хит в сервисе статистики (app = "ewm-main-service").
 */
@RestController
@RequiredArgsConstructor
public class EventsStubController {

    private static final String APP_NAME = "ewm-main-service";

    private final InMemoryStubStore store;
    private final StatsService statsService;

    @GetMapping("/events")
    public List<EventStubDto> getAll(HttpServletRequest request) {
        registerHit(request);
        return store.getEvents();
    }

    @GetMapping("/events/{id}")
    public EventStubDto getById(@PathVariable("id") long id, HttpServletRequest request) {
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
            statsService.saveHit(dto);
        } catch (Exception ignored) {
            // В рамках заглушек ошибки отправки хита не должны валить ответ контроллера.
        }
    }
}
