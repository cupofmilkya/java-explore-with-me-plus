package ru.practicum.web.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.web.admin.dto.UserShortDto;
import ru.practicum.web.comment.dto.CommentDto;
import ru.practicum.web.comment.entity.Comment;
import ru.practicum.web.validation.ValidationConstants;

import java.time.format.DateTimeFormatter;

/**
 * Маппер для преобразования сущности комментария в DTO и обратно.
 */
@UtilityClass
public class CommentMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(ValidationConstants.DATE_TIME_FORMAT);

    public static CommentDto toDto(Comment comment) {
        if (comment == null) return null;
        return CommentDto.builder()
                .id(comment.getId())
                .eventId(comment.getEvent().getId())
                .author(new UserShortDto(comment.getAuthor().getId(), comment.getAuthor().getName()))
                .text(comment.getText())
                .createdOn(comment.getCreatedOn() != null ? comment.getCreatedOn().format(FORMATTER) : null)
                .moderationStatus(comment.getModerationStatus() != null ? comment.getModerationStatus().name() : null)
                .build();
    }
}
