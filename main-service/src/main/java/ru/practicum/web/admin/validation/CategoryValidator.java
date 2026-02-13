package ru.practicum.web.admin.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.web.admin.repository.CategoryRepository;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.exception.NotFoundException;
import ru.practicum.web.validation.ValidationConstants;

@Component
@RequiredArgsConstructor
public class CategoryValidator {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public void validateCategoryName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }
        if (name.length() > ValidationConstants.CATEGORY_NAME_MAX) {
            throw new BadRequestException("Category name length must be between 1 and " +
                    ValidationConstants.CATEGORY_NAME_MAX + " characters");
        }
    }

    public void validateCategoryExists(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
    }

    public void checkCategoryNameUnique(String name) {
        if (categoryRepository.existsByName(name)) {
            throwConflictException();
        }
    }

    public void checkCategoryNameUniqueForUpdate(String name, Long id) {
        if (categoryRepository.existsByNameAndIdNot(name, id)) {
            throwConflictException();
        }
    }

    public void checkCategoryNotInUse(Long categoryId) {
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("The category is not empty");
        }
    }

    public void validatePagination(int from, int size) {
        if (from < ValidationConstants.PAGE_MIN_FROM) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }
        if (size < ValidationConstants.PAGE_MIN_SIZE) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }
    }

    private void throwConflictException() {
        throw new ConflictException(
                "could not execute statement; SQL [n/a]; constraint [uq_category_name]; " +
                        "nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement"
        );
    }
}