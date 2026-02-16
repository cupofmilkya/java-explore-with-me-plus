package ru.practicum.web.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.user.entity.User;

import java.time.LocalDateTime;

/**
 * Сущность комментария к событию.
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Автор комментария */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** Событие, к которому относится комментарий */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Текст комментария */
    @Column(nullable = false, length = 2000)
    private String text;

    /** Дата и время создания комментария */
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    /** Статус модерации комментария */
    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private CommentModerationStatus moderationStatus = CommentModerationStatus.PENDING;
}
