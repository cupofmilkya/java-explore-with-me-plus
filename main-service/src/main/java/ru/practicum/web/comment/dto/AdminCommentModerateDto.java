package ru.practicum.web.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для модерации комментария администратором.
 * Требуется только идентификатор комментария и новый статус модерации.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentModerateDto {
    /** Идентификатор комментария */
    @NotNull
    private Long commentId;

    /** Новый статус модерации: PENDING, APPROVED, REJECTED */
    @NotNull
    private String status;
}
