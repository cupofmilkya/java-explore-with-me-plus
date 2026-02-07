package ru.practicum.web.admin.service;

import ru.practicum.web.admin.dto.CategoryDto;

import java.util.List;

public interface AdminCategoryService {

    CategoryDto create(CategoryDto dto);

    CategoryDto update(Long id, CategoryDto dto);

    void delete(Long id);

    List<CategoryDto> getAll(int from, int size);
}