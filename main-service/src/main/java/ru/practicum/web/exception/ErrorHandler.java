package ru.practicum.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFoundHandler(NotFoundException e) {
        log.error("NotFoundException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND.name())
                .reason("The required object was not found.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(Collections.emptyList())
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflictHandler(ConflictException e) {
        log.error("ConflictException: {}", e.getMessage());
        String reason = e.getMessage().contains("constraint")
                ? "Integrity constraint has been violated."
                : "For the requested operation the conditions are not met.";

        return ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason(reason)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(Collections.emptyList())
                .build();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequestHandler(BadRequestException e) {
        log.error("BadRequestException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(Collections.emptyList())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError illegalArgumentHandler(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(Collections.emptyList())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationExceptions(MethodArgumentNotValidException e) {  // Измените имя метода
        log.error("MethodArgumentNotValidException: {}", e.getMessage());
        String message = "Validation error";
        if (e.getBindingResult().getFieldError() != null) {
            message = "Field: " + e.getBindingResult().getFieldError().getField() +
                    ". Error: " + e.getBindingResult().getFieldError().getDefaultMessage() +
                    ". Value: " + e.getBindingResult().getFieldError().getRejectedValue();
        }

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request.")
                .message(message)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(Collections.emptyList())
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError dataIntegrityHandler(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException: {}", e.getMessage());
        String message = e.getMostSpecificCause().getMessage();
        String reason = "Integrity constraint has been violated.";

        if (message.contains("uq_category_name")) {
            message = "could not execute statement; SQL [n/a]; constraint [uq_category_name]; " +
                    "nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement";
        } else if (message.contains("uq_email")) {
            message = "could not execute statement; SQL [n/a]; constraint [uq_email]; " +
                    "nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement";
        } else if (message.contains("uq_compilation_name")) {
            message = "could not execute statement; SQL [n/a]; constraint [uq_compilation_name]; " +
                    "nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement";
        } else if (message.contains("uq_request")) {
            message = "could not execute statement; SQL [n/a]; constraint [uq_request]; " +
                    "nested exception is org.hibernate.exception.ConstraintViolationException: could not execute statement";
        }

        return ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(Collections.emptyList())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError genericHandler(Exception e) {
        log.error("Internal server error: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Internal server error.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .errors(Collections.emptyList())
                .build();
    }
}