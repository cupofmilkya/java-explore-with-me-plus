package ru.practicum.web.admin.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.web.admin.dto.NewCompilationDto;
import ru.practicum.web.admin.entity.UpdateCompilationRequest;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.NotFoundException;
import ru.practicum.web.validation.ValidationConstants;

@Component
@RequiredArgsConstructor
public class CompilationValidator {

    public void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Title must not be blank");
        }
        if (title.length() < ValidationConstants.COMPILATION_TITLE_MIN ||
                title.length() > ValidationConstants.COMPILATION_TITLE_MAX) {
            throw new BadRequestException("Title length must be between " +
                    ValidationConstants.COMPILATION_TITLE_MIN + " and " +
                    ValidationConstants.COMPILATION_TITLE_MAX + " characters");
        }
    }

    public void validateTitleForUpdate(String title) {
        if (title != null) {
            if (title.isBlank()) {
                throw new BadRequestException("Title must not be blank");
            }
            if (title.length() < ValidationConstants.COMPILATION_TITLE_MIN ||
                    title.length() > ValidationConstants.COMPILATION_TITLE_MAX) {
                throw new BadRequestException("Title length must be between " +
                        ValidationConstants.COMPILATION_TITLE_MIN + " and " +
                        ValidationConstants.COMPILATION_TITLE_MAX + " characters");
            }
        }
    }

    public void validateCompilationExists(Boolean exists, Long id) {
        if (!exists) {
            throw new NotFoundException("Compilation with id=" + id + " was not found");
        }
    }

    public void validateCreateRequest(NewCompilationDto dto) {
        validateTitle(dto.getTitle());
        // Другие валидации при необходимости
    }

    public void validateUpdateRequest(UpdateCompilationRequest dto) {
        validateTitleForUpdate(dto.getTitle());
        // Другие валидации при необходимости
    }
}