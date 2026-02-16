package ru.practicum.web.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания нового комментария.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    /**
     * Текст комментария. Ограничение по длине: 1..2000 символов.
     */
    @NotBlank
    @Size(min = 1, max = 2000)
    private String text;
}
