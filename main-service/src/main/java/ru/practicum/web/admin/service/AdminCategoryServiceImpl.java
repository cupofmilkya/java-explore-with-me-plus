package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.admin.dto.CategoryDto;
import ru.practicum.web.admin.entity.Category;
import ru.practicum.web.admin.mapper.CategoryMapper;
import ru.practicum.web.admin.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository repository;

    @Override
    public CategoryDto create(CategoryDto dto) {
        Category category = CategoryMapper.toEntity(dto);
        return CategoryMapper.toDto(repository.save(category));
    }

    @Override
    public CategoryDto update(Long id, CategoryDto dto) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id " + id));
        category.setName(dto.getName());
        return CategoryMapper.toDto(repository.save(category));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        List<Category> categories = repository.findAll();
        int start = Math.min(from, categories.size());
        int end = Math.min(start + size, categories.size());
        return categories.subList(start, end).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }
}