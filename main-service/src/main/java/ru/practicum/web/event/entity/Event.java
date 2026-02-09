package ru.practicum.web.event.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.web.admin.entity.Category;
import ru.practicum.web.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String annotation;
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private Boolean paid;

    private Integer participantLimit;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Transient
    private Long views = 0L;

    @Column(name = "confirmed_requests")
    private Long confirmedRequests = 0L;

    public enum Status {
        PENDING, PUBLISHED, CANCELED
    }
}