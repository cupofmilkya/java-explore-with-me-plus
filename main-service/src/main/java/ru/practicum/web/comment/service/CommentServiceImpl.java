package ru.practicum.web.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.web.comment.dto.CommentDto;
import ru.practicum.web.comment.dto.NewCommentDto;
import ru.practicum.web.comment.dto.UpdateCommentDto;
import ru.practicum.web.comment.entity.Comment;
import ru.practicum.web.comment.entity.CommentModerationStatus;
import ru.practicum.web.comment.mapper.CommentMapper;
import ru.practicum.web.comment.repository.CommentRepository;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.event.entity.EventStatus;
import ru.practicum.web.event.repository.EventRepository;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.NotFoundException;
import ru.practicum.web.user.entity.User;
import ru.practicum.web.user.repository.UserRepository;
import ru.practicum.web.validation.ValidationConstants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CommentDto> getApprovedCommentsForEvent(Long eventId, int from, int size) {
        log.info("Запрос списка одобренных комментариев для события id={}", eventId);
        ensureEventPublished(eventId);
        if (from < ValidationConstants.PAGE_MIN_FROM) {
            throw new BadRequestException("Parameter 'from' must be non-negative");
        }
        if (size <= 0) {
            throw new BadRequestException("Parameter 'size' must be positive");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        return commentRepository.findByEventIdAndModerationStatus(eventId, CommentModerationStatus.APPROVED, pageable)
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getApprovedComment(Long eventId, Long commentId) {
        log.info("Запрос одобренного комментария id={} для события id={}", commentId, eventId);
        ensureEventPublished(eventId);
        Comment comment = commentRepository.findByIdAndEventId(commentId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        if (comment.getModerationStatus() != CommentModerationStatus.APPROVED) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }
        return CommentMapper.toDto(comment);
    }

    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto dto) {
        log.info("Создание комментария к событию id={} пользователем id={}", eventId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Comment comment = Comment.builder()
                .author(user)
                .event(event)
                .text(dto.getText())
                .createdOn(LocalDateTime.now().withNano(0))
                .moderationStatus(CommentModerationStatus.PENDING)
                .build();

        Comment saved = commentRepository.save(comment);
        log.debug("Комментарий создан с id={}, статус модерации={} ", saved.getId(), saved.getModerationStatus());
        return CommentMapper.toDto(saved);
    }

    @Override
    public CommentDto updateOwnComment(Long userId, Long eventId, Long commentId, UpdateCommentDto dto) {
        log.info("Обновление комментария id={} пользователем id={} для события id={}", commentId, userId, eventId);
        ensureUserExists(userId);
        ensureEventExists(eventId);
        Comment comment = commentRepository.findByIdAndAuthorIdAndEventId(commentId, userId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        comment.setText(dto.getText());
        // При изменении текста отправляем на повторную модерацию
        comment.setModerationStatus(CommentModerationStatus.PENDING);
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    @Override
    public void deleteOwnComment(Long userId, Long eventId, Long commentId) {
        log.info("Удаление комментария id={} пользователем id={} для события id={}", commentId, userId, eventId);
        ensureUserExists(userId);
        ensureEventExists(eventId);
        Comment comment = commentRepository.findByIdAndAuthorIdAndEventId(commentId, userId, eventId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        commentRepository.deleteById(comment.getId());
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }

    private void ensureEventExists(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
    }

    private void ensureEventPublished(Long eventId) {
        eventRepository.findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
