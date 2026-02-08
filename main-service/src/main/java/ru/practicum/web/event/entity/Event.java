package ru.practicum.web.event.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.web.admin.entity.Category;

import java.time.LocalDateTime;

@Entity
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String annotation;

    private LocalDateTime eventDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING,
        PUBLISHED,
        CANCELED
    }
}