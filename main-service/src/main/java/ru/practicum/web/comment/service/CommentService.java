package ru.practicum.web.comment.service;

import ru.practicum.web.comment.dto.CommentDto;
import ru.practicum.web.comment.dto.NewCommentDto;
import ru.practicum.web.comment.dto.UpdateCommentDto;

import java.util.List;

/**
 * Сервис для работы с комментариями пользователей.
 */
public interface CommentService {

    /**
     * Получить список одобренных комментариев для события (публично).
     */
    List<CommentDto> getApprovedCommentsForEvent(Long eventId, int from, int size);

    /**
     * Получить одобренный комментарий по идентификатору (публично).
     */
    CommentDto getApprovedComment(Long eventId, Long commentId);

    /**
     * Создать комментарий от имени пользователя к событию.
     */
    CommentDto addComment(Long userId, Long eventId, NewCommentDto dto);

    /**
     * Обновить собственный комментарий.
     */
    CommentDto updateOwnComment(Long userId, Long eventId, Long commentId, UpdateCommentDto dto);

    /**
     * Удалить собственный комментарий.
     */
    void deleteOwnComment(Long userId, Long eventId, Long commentId);
}
