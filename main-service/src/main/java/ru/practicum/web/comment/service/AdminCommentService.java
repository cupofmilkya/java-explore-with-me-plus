package ru.practicum.web.comment.service;

import ru.practicum.web.comment.dto.AdminCommentModerateDto;
import ru.practicum.web.comment.dto.CommentDto;

/**
 * Сервис модерации комментариев для администраторов.
 */
public interface AdminCommentService {

    /**
     * Обновить статус модерации комментария.
     */
    CommentDto moderate(AdminCommentModerateDto dto);
}
