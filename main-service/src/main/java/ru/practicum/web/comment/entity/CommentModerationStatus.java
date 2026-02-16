package ru.practicum.web.comment.entity;

/**
 * Статус модерации комментария.
 */
public enum CommentModerationStatus {
    /** Комментарий ожидает модерации */
    PENDING,
    /** Комментарий одобрен и виден публично */
    APPROVED,
    /** Комментарий отклонён и не отображается публично */
    REJECTED
}
