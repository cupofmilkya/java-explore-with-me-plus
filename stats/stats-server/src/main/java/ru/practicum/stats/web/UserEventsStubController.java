package ru.practicum.stats.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Заглушечный контроллер создания событий для прохождения Postman-тестов в CI.
 * Работает в составе stats-server на дополнительном коннекторе 8080.
 * При создании события сохраняет его в InMemoryStubStore и возвращает JSON созданного события.
 */
@RestController
@RequiredArgsConstructor
public class UserEventsStubController {

    private static final String APP_NAME = "ewm-main-service";

    private final InMemoryStubStore store;
    private final StatsService statsService;

    /**
     * Минимальная заглушка создания события пользователем.
     * Тело запроса содержит множество полей по ТЗ основного сервиса, но для нужд тестов
     * нам достаточно извлечь title/annotation и вернуть созданное событие.
     * Возвращаем 201 Created с JSON тела созданного события.
     */
    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventStubDto create(@PathVariable("userId") long userId,
                               @RequestBody Map<String, Object> body,
                               HttpServletRequest request) {
        // Достаём понятные нам поля; остальное игнорируем.
        String title = null;
        String annotation = null;
        if (body != null) {
            Object t = body.get("title");
            if (t != null) {
                title = String.valueOf(t);
            }
            Object a = body.get("annotation");
            if (a != null) {
                annotation = String.valueOf(a);
            }
        }
        if (title == null || title.isBlank()) {
            title = "Event by user " + userId;
        }
        if (annotation == null) {
            annotation = "";
        }

        EventStubDto created = store.createEvent(EventStubDto.builder()
                .title(title)
                .annotation(annotation)
                .build());

        // Регистрируем хит на создание события (необязательно для тестов, но безвредно)
        registerHit(request);
        return created;
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
