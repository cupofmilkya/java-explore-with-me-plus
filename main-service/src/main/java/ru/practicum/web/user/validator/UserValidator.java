package ru.practicum.web.user.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.exception.NotFoundException;
import ru.practicum.web.user.dto.NewUserRequest;
import ru.practicum.web.user.repository.UserRepository;
import ru.practicum.web.validation.ValidationConstants;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateCreateRequest(NewUserRequest request) {
        validateEmail(request.getEmail());
        validateName(request.getName());
        checkEmailUnique(request.getEmail());
    }

    public void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email must not be blank");
        }
        if (email.length() < ValidationConstants.USER_EMAIL_MIN ||
                email.length() > ValidationConstants.USER_EMAIL_MAX) {
            throw new BadRequestException("Email length must be between " +
                    ValidationConstants.USER_EMAIL_MIN + " and " + ValidationConstants.USER_EMAIL_MAX + " characters");
        }
        if (!email.contains("@")) {
            throw new BadRequestException("Invalid email format");
        }
    }

    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Name must not be blank");
        }
        if (name.length() < ValidationConstants.USER_NAME_MIN ||
                name.length() > ValidationConstants.USER_NAME_MAX) {
            throw new BadRequestException("Name length must be between " +
                    ValidationConstants.USER_NAME_MIN + " and " + ValidationConstants.USER_NAME_MAX + " characters");
        }
    }

    public void checkEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email " + email + " already exists");
        }
    }

    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
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
}