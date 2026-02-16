package ru.practicum.web.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.web.comment.entity.Comment;
import ru.practicum.web.comment.entity.CommentModerationStatus;

import java.util.Optional;

/**
 * Репозиторий для работы с комментариями.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByEventIdAndModerationStatus(Long eventId, CommentModerationStatus status, Pageable pageable);

    Optional<Comment> findByIdAndEventId(Long id, Long eventId);

    Optional<Comment> findByIdAndAuthorIdAndEventId(Long id, Long authorId, Long eventId);

    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);
}
