package ru.practicum.web.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.web.comment.dto.CommentDto;
import ru.practicum.web.comment.dto.NewCommentDto;
import ru.practicum.web.comment.dto.UpdateCommentDto;
import ru.practicum.web.comment.service.CommentService;

/**
 * Закрытые (private) эндпоинты для авторов комментариев.
 */
@Slf4j
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
@Validated
public class PrivateCommentController {

    private final CommentService commentService;

    /**
     * Создать комментарий к событию от имени пользователя.
     */
    @PostMapping
    public ResponseEntity<CommentDto> addComment(@PathVariable Long userId,
                                                 @PathVariable Long eventId,
                                                 @RequestBody @Valid NewCommentDto dto) {
        log.info("POST /users/{}/events/{}/comments", userId, eventId);
        CommentDto created = commentService.addComment(userId, eventId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновить собственный комментарий.
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @PathVariable Long commentId,
                                                    @RequestBody @Valid UpdateCommentDto dto) {
        log.info("PATCH /users/{}/events/{}/comments/{}", userId, eventId, commentId);
        return ResponseEntity.ok(commentService.updateOwnComment(userId, eventId, commentId, dto));
    }

    /**
     * Удалить собственный комментарий.
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long userId,
                                              @PathVariable Long eventId,
                                              @PathVariable Long commentId) {
        log.info("DELETE /users/{}/events/{}/comments/{}", userId, eventId, commentId);
        commentService.deleteOwnComment(userId, eventId, commentId);
        return ResponseEntity.noContent().build();
    }
}
