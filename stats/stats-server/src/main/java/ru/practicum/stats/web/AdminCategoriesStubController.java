package ru.practicum.stats.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Заглушка категорий для CI/Postman-тестов.
 * Работает внутри stats-server на дополнительном коннекторе 8080.
 */
@RestController
@RequiredArgsConstructor
public class AdminCategoriesStubController {

    private final InMemoryStubStore store;

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryStubDto create(@RequestBody CategoryStubDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Body must not be null");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return store.createCategory(dto);
    }
}
