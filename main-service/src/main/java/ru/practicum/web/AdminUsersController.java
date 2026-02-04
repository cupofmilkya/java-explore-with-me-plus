package ru.practicum.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.web.dto.UserDto;

/**
 * Минимальная заглушка для POST /admin/users, необходимая для Postman-тестов.
 */
@RestController
@RequiredArgsConstructor
public class AdminUsersController {

    private final InMemoryStore store;

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto dto) {
        // Простейшая валидация по месту (без Bean Validation, чтобы не подтягивать лишние зависимости)
        if (dto == null) {
            throw new IllegalArgumentException("Body must not be null");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        return store.createUser(dto);
    }
}
