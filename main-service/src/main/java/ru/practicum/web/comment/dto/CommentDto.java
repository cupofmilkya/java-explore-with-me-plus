package ru.practicum.web.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.web.admin.dto.UserShortDto;

/**
 * DTO комментария для отдачи наружу.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private Long eventId;
    private UserShortDto author;
    private String text;
    private String createdOn;
    private String moderationStatus;
}
