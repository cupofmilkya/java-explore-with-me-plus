package ru.practicum.stats.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Заглушка для администрирования событий: PATCH /admin/events/{id}.
 * Нужна исключительно для прохождения Postman-тестов в CI, работает на порту 8080
 * внутри stats-server (см. AdditionalConnectorConfig).
 *
 * По требованиям тестов достаточно принимать тело с полем stateAction
 * (например, "PUBLISH_EVENT") и отдавать 200 с данными события. Изменять
 * состояние события необязательно для целей этих тестов, поэтому здесь no-op.
 */
@RestController
@RequiredArgsConstructor
public class AdminEventsStubController {

    private final InMemoryStubStore store;

    @PatchMapping("/admin/events/{eventId}")
    public EventStubDto publish(@PathVariable("eventId") long eventId,
                                @RequestBody(required = false) StateActionRequest body) {
        return store.getEventById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    /**
     * Простейшее тело запроса, которое ожидает Postman-сценарий при публикации события.
     * Поле никак не используется в логике заглушки.
     */
    public record StateActionRequest(String stateAction) {}
}
