package ru.practicum.web.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.web.comment.dto.AdminCommentModerateDto;
import ru.practicum.web.comment.dto.CommentDto;
import ru.practicum.web.comment.service.AdminCommentService;

/**
 * Административные эндпоинты для модерации комментариев.
 */
@Slf4j
@RestController
@RequestMapping("/admin/events/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    /**
     * Обновить статус модерации комментария.
     */
    @PutMapping
    public ResponseEntity<CommentDto> moderate(@RequestBody @Valid AdminCommentModerateDto dto) {
        log.info("PUT /admin/events/comments - moderate {} -> {}", dto.getCommentId(), dto.getStatus());
        return ResponseEntity.ok(adminCommentService.moderate(dto));
    }
}
