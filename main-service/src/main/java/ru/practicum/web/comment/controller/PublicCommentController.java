package ru.practicum.web.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.web.comment.dto.CommentDto;
import ru.practicum.web.comment.service.CommentService;
import ru.practicum.web.exception.BadRequestException;

import java.util.List;

/**
 * Публичные эндпоинты для чтения комментариев к событиям.
 */
@Slf4j
@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
@Validated
public class PublicCommentController {

    private final CommentService commentService;

    /**
     * Получить одобренные комментарии к событию с пагинацией.
     */
    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long eventId,
                                                        @RequestParam(defaultValue = "0") int from,
                                                        @RequestParam(defaultValue = "10") int size) {
        log.info("GET /events/{}/comments from={}, size={}", eventId, from, size);
        if (from < 0) throw new BadRequestException("Parameter 'from' must be non-negative");
        if (size <= 0) throw new BadRequestException("Parameter 'size' must be positive");
        return ResponseEntity.ok(commentService.getApprovedCommentsForEvent(eventId, from, size));
    }

    /**
     * Получить одобренный комментарий по id.
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long eventId, @PathVariable Long commentId) {
        log.info("GET /events/{}/comments/{}", eventId, commentId);
        return ResponseEntity.ok(commentService.getApprovedComment(eventId, commentId));
    }
}
