package ru.practicum.web.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.web.admin.dto.CategoryDto;
import ru.practicum.web.admin.service.AdminCategoryService;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoriesController {

    private final AdminCategoryService service;

    @PostMapping
    public ResponseEntity<CategoryDto> create(@RequestBody @Validated CategoryDto dto) {
        CategoryDto created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> update(
            @PathVariable Long catId,
            @RequestBody @Validated CategoryDto dto
    ) {
        CategoryDto updated = service.update(catId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> delete(@PathVariable Long catId) {
        service.delete(catId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAll(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<CategoryDto> categories = service.getAll(from, size);
        return ResponseEntity.ok(categories);
    }
}