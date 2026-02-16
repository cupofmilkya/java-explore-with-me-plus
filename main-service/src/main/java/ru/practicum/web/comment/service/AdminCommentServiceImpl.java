package ru.practicum.web.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.comment.dto.AdminCommentModerateDto;
import ru.practicum.web.comment.dto.CommentDto;
import ru.practicum.web.comment.entity.Comment;
import ru.practicum.web.comment.entity.CommentModerationStatus;
import ru.practicum.web.comment.mapper.CommentMapper;
import ru.practicum.web.comment.repository.CommentRepository;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.NotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminCommentServiceImpl implements AdminCommentService {

    private final CommentRepository commentRepository;

    @Override
    public CommentDto moderate(AdminCommentModerateDto dto) {
        log.info("Модерация комментария id={} -> статус {}", dto.getCommentId(), dto.getStatus());
        Comment comment = commentRepository.findById(dto.getCommentId())
                .orElseThrow(() -> new NotFoundException("Comment with id=" + dto.getCommentId() + " was not found"));

        CommentModerationStatus status;
        try {
            status = CommentModerationStatus.valueOf(dto.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid moderation status: " + dto.getStatus());
        }

        comment.setModerationStatus(status);
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }
}
