package ru.practicum.stats.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Заглушка контроллера администрирования пользователей для нужд Postman-тестов в CI.
 * Работает в составе stats-server на дополнительном коннекторе 8080.
 */
@RestController
@RequiredArgsConstructor
public class AdminUsersStubController {

    private final InMemoryStubStore store;

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserStubDto create(@RequestBody UserStubDto dto) {
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
