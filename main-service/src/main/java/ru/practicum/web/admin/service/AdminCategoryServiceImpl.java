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
        if (!repository.existsById(id)) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }

         if (eventRepository.existsByCategoryId(id)) {
             throw new ConflictException("The category is not empty");
         }

        repository.deleteById(id);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        // Правильная пагинация через Pageable
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return repository.findAll(pageable)
                .stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }
}