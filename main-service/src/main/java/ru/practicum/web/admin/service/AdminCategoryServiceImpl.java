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
import ru.practicum.web.admin.validation.CategoryValidator;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryValidator validator;

    @Override
    public CategoryDto create(NewCategoryDto dto) {
        validator.validateCategoryName(dto.getName());
        validator.checkCategoryNameUnique(dto.getName());

        try {
            Category category = Category.builder()
                    .name(dto.getName())
                    .build();
            return CategoryMapper.toDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            validator.checkCategoryNameUnique(dto.getName());
            throw e;
        }
    }

    @Override
    public CategoryDto update(Long id, CategoryDto dto) {
        validator.validateCategoryName(dto.getName());
        validator.validateCategoryExists(id);
        validator.checkCategoryNameUniqueForUpdate(dto.getName(), id);

        Category category = getCategoryOrThrow(id);
        category.setName(dto.getName());

        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        validator.validateCategoryExists(id);
        checkCategoryNotInUse(id);

        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        validator.validatePagination(from, size);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        return categoryRepository.findAll(pageable)
                .stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getById(Long id) {
        return CategoryMapper.toDto(getCategoryOrThrow(id));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found"));
    }

    private void checkCategoryNotInUse(Long categoryId) {
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("The category is not empty");
        }
    }
}