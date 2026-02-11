package ru.practicum.web.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.admin.dto.CategoryDto;
import ru.practicum.web.admin.dto.NewCategoryDto;
import ru.practicum.web.admin.entity.Category;
import ru.practicum.web.admin.mapper.CategoryMapper;
import ru.practicum.web.admin.repository.CategoryRepository;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto create(NewCategoryDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }
        if (dto.getName().length() > 50) {
            throw new BadRequestException("Category name length must be between 1 and 50 characters");
        }

        if (repository.existsByName(dto.getName())) {
            throw new ConflictException(
                    "could not execute statement; SQL [n/a]; constraint [uq_category_name]; " +
                            "nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement"
            );
        }

        try {
            Category category = Category.builder()
                    .name(dto.getName())
                    .build();
            return CategoryMapper.toDto(repository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(
                    "could not execute statement; SQL [n/a]; constraint [uq_category_name]; " +
                            "nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement"
            );
        }
    }

    @Override
    public CategoryDto update(Long id, CategoryDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }
        if (dto.getName().length() > 50) {
            throw new BadRequestException("Category name length must be between 1 and 50 characters");
        }

        Category category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found"));

        if (!category.getName().equals(dto.getName()) &&
                repository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new ConflictException(
                    "could not execute statement; SQL [n/a]; constraint [uq_category_name]; " +
                            "nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement"
            );
        }

        category.setName(dto.getName());
        return CategoryMapper.toDto(repository.save(category));
    }

    @Override
    public void delete(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found"));

        if (eventRepository.existsByCategoryId(id)) {
            throw new ConflictException("The category is not empty");
        }

        repository.delete(category);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        if (from < 0) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return repository.findAll(pageable)
                .stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long id) {
        return repository.findById(id)
                .map(CategoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found"));
    }
}