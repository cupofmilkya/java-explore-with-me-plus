package ru.practicum.base.mapper;

import ru.practicum.base.dto.category.CategoryDto;
import ru.practicum.base.model.Category;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryMapper {
    public static Category toEntity(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName()).build();
    }

    public static CategoryDto toDto(Category entity) {
        return CategoryDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public static List<CategoryDto> toDtoList(List<Category> categories) {
        return categories.stream().map(CategoryMapper::toDto).collect(Collectors.toList());
    }
}
